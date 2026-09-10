package com.mmsp.service;

import com.mmsp.model.Device;
import com.mmsp.model.GlobalScenario;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStateStore implements StateStore {

    private final Map<String, Device> devices = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> commands = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> telemetry = new ConcurrentHashMap<>();
    private volatile GlobalScenario globalScenario = new GlobalScenario("success", 50, Instant.now().toString());

    @Override
    public GlobalScenario setGlobalScenario(String scenario, int latencyMs) {
        globalScenario = new GlobalScenario(scenario, latencyMs, Instant.now().toString());
        return globalScenario;
    }

    @Override
    public GlobalScenario getGlobalScenario() {
        return globalScenario;
    }

    @Override
    public Device saveDevice(Device device) {
        devices.put(device.getDeviceId(), device);
        return device;
    }

    @Override
    public Optional<Device> getDevice(String deviceId) {
        return Optional.ofNullable(devices.get(deviceId));
    }

    @Override
    public List<Device> listDevices() {
        return new ArrayList<>(devices.values());
    }

    @Override
    public Optional<Device> updateDevice(String deviceId, DeviceUpdater updater) {
        Device current = devices.get(deviceId);
        if (current == null) {
            return Optional.empty();
        }
        updater.apply(current);
        current.setUpdatedAt(Instant.now().toString());
        devices.put(deviceId, current);
        return Optional.of(current);
    }

    @Override
    public void saveCommand(String commandId, Map<String, Object> command) {
        commands.put(commandId, command);
    }

    @Override
    public void saveTelemetry(String telemetryId, Map<String, Object> record) {
        telemetry.put(telemetryId, record);
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("devices", devices.size());
        stats.put("commands", commands.size());
        stats.put("telemetry", telemetry.size());
        stats.put("globalScenario", globalScenario.getScenario());
        stats.put("backend", backend());
        return stats;
    }

    @Override
    public boolean healthy() {
        return true;
    }

    @Override
    public String backend() {
        return "memory";
    }
}
