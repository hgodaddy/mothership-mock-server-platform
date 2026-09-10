package com.mmsp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmsp.model.Device;
import com.mmsp.model.GlobalScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RedisStateStore implements StateStore {

    private static final Logger log = LoggerFactory.getLogger(RedisStateStore.class);

    static final String KEY_GLOBAL_SCENARIO = "mmsp:scenario:global";
    static final String KEY_DEVICE_INDEX = "mmsp:devices";
    static final String KEY_COMMAND_INDEX = "mmsp:commands";
    static final String KEY_TELEMETRY_INDEX = "mmsp:telemetry";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RedisStateStore(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    public GlobalScenario setGlobalScenario(String scenario, int latencyMs) {
        GlobalScenario global = new GlobalScenario(scenario, latencyMs, Instant.now().toString());
        redis.opsForValue().set(KEY_GLOBAL_SCENARIO, write(global));
        return global;
    }

    @Override
    public GlobalScenario getGlobalScenario() {
        String raw = redis.opsForValue().get(KEY_GLOBAL_SCENARIO);
        if (raw == null || raw.isBlank()) {
            return new GlobalScenario("success", 50, Instant.now().toString());
        }
        return read(raw, GlobalScenario.class);
    }

    @Override
    public Device saveDevice(Device device) {
        redis.opsForValue().set(deviceKey(device.getDeviceId()), write(device));
        redis.opsForSet().add(KEY_DEVICE_INDEX, device.getDeviceId());
        return device;
    }

    @Override
    public Optional<Device> getDevice(String deviceId) {
        String raw = redis.opsForValue().get(deviceKey(deviceId));
        if (raw == null) {
            return Optional.empty();
        }
        return Optional.of(read(raw, Device.class));
    }

    @Override
    public List<Device> listDevices() {
        Set<String> ids = redis.opsForSet().members(KEY_DEVICE_INDEX);
        List<Device> devices = new ArrayList<>();
        if (ids == null) {
            return devices;
        }
        for (String id : ids) {
            getDevice(id).ifPresent(devices::add);
        }
        return devices;
    }

    @Override
    public Optional<Device> updateDevice(String deviceId, DeviceUpdater updater) {
        Optional<Device> current = getDevice(deviceId);
        if (current.isEmpty()) {
            return Optional.empty();
        }
        Device device = current.get();
        updater.apply(device);
        device.setUpdatedAt(Instant.now().toString());
        return Optional.of(saveDevice(device));
    }

    @Override
    public void saveCommand(String commandId, Map<String, Object> command) {
        redis.opsForValue().set(commandKey(commandId), write(command));
        redis.opsForSet().add(KEY_COMMAND_INDEX, commandId);
    }

    @Override
    public void saveTelemetry(String telemetryId, Map<String, Object> record) {
        redis.opsForValue().set(telemetryKey(telemetryId), write(record));
        redis.opsForSet().add(KEY_TELEMETRY_INDEX, telemetryId);
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("devices", size(KEY_DEVICE_INDEX));
        stats.put("commands", size(KEY_COMMAND_INDEX));
        stats.put("telemetry", size(KEY_TELEMETRY_INDEX));
        stats.put("globalScenario", getGlobalScenario().getScenario());
        stats.put("backend", backend());
        return stats;
    }

    @Override
    public boolean healthy() {
        try {
            var factory = redis.getConnectionFactory();
            if (factory == null) {
                return false;
            }
            try (var connection = factory.getConnection()) {
                String pong = connection.ping();
                return pong != null && pong.equalsIgnoreCase("PONG");
            }
        } catch (Exception ex) {
            log.warn("Redis health check failed: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public String backend() {
        return "redis";
    }

    private long size(String key) {
        Long size = redis.opsForSet().size(key);
        return size == null ? 0L : size;
    }

    private static String deviceKey(String deviceId) {
        return "mmsp:device:" + deviceId;
    }

    private static String commandKey(String commandId) {
        return "mmsp:command:" + commandId;
    }

    private static String telemetryKey(String telemetryId) {
        return "mmsp:telemetry:" + telemetryId;
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize MMSP state for Redis", ex);
        }
    }

    private <T> T read(String raw, Class<T> type) {
        try {
            return objectMapper.readValue(raw, type);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize MMSP state from Redis", ex);
        }
    }
}
