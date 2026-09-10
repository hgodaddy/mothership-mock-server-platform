package com.mmsp.service;

import com.mmsp.config.MmspProperties;
import com.mmsp.dto.CommandRequest;
import com.mmsp.dto.DeviceRegisterRequest;
import com.mmsp.dto.HeartbeatRequest;
import com.mmsp.dto.TelemetryRequest;
import com.mmsp.exception.MmspException;
import com.mmsp.model.Device;
import com.mmsp.util.Ids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DeviceSimulator {

    private static final Logger log = LoggerFactory.getLogger(DeviceSimulator.class);

    private final MmspProperties properties;
    private final StateStore stateStore;

    public DeviceSimulator(MmspProperties properties, StateStore stateStore) {
        this.properties = properties;
        this.stateStore = stateStore;
    }

    public Device register(DeviceRegisterRequest request) {
        String now = Instant.now().toString();
        Device device = new Device();
        device.setDeviceId(Ids.shortId("dev"));
        device.setDeviceSerial(request.getDeviceSerial());
        device.setDeviceType(request.getDeviceType());
        device.setFirmwareVersion(request.getFirmwareVersion());
        device.setSiteId(request.getSiteId());
        device.setMetadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>());
        device.setStatus("registered");
        device.setState("online");
        device.setRegisteredAt(now);
        device.setUpdatedAt(now);
        stateStore.saveDevice(device);
        log.info("Device registered deviceId={} serial={}", device.getDeviceId(), device.getDeviceSerial());
        return device;
    }

    public Map<String, Object> updateStatus(String deviceId, String state, String reason) {
        if (!properties.getDeviceStates().contains(state)) {
            throw new MmspException(
                    400,
                    "INVALID_STATE",
                    "Invalid state '" + state + "'. Allowed: " + String.join(", ", properties.getDeviceStates())
            );
        }
        Device current = requireDevice(deviceId);
        String previous = current.getState();
        Device updated = stateStore.updateDevice(deviceId, d -> {
            d.setState(state);
            d.setStatusReason(reason);
        }).orElseThrow();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deviceId", updated.getDeviceId());
        result.put("previousState", previous);
        result.put("state", updated.getState());
        result.put("updatedAt", updated.getUpdatedAt());
        return result;
    }

    public Device heartbeat(String deviceId, HeartbeatRequest request) {
        Device current = requireDevice(deviceId);
        String timestamp = request != null && request.getTimestamp() != null
                ? request.getTimestamp()
                : Instant.now().toString();
        Map<String, Object> metrics = new HashMap<>();
        if (request != null) {
            metrics.put("cpuPercent", request.getCpuPercent());
            metrics.put("memoryPercent", request.getMemoryPercent());
        }
        return stateStore.updateDevice(deviceId, d -> {
            d.setLastHeartbeatAt(timestamp);
            d.setLastHeartbeatMetrics(metrics);
            if (!"offline".equals(current.getState()) && d.getState() == null) {
                d.setState("online");
            }
        }).orElseThrow();
    }

    public Map<String, Object> acceptCommand(String deviceId, CommandRequest request) {
        requireDevice(deviceId);
        String commandId = Ids.shortId("cmd");
        String correlationId = request.getCorrelationId() != null
                ? request.getCorrelationId()
                : Ids.shortId("corr");
        String acceptedAt = Instant.now().toString();
        Map<String, Object> command = new LinkedHashMap<>();
        command.put("commandId", commandId);
        command.put("deviceId", deviceId);
        command.put("commandType", request.getCommandType());
        command.put("correlationId", correlationId);
        command.put("payload", request.getPayload() != null ? request.getPayload() : Map.of());
        command.put("status", "accepted");
        command.put("acceptedAt", acceptedAt);
        stateStore.saveCommand(commandId, command);
        stateStore.updateDevice(deviceId, d -> {
            d.setState("busy");
            d.setLastCommandId(commandId);
        });
        return command;
    }

    public Map<String, Object> acceptTelemetry(String deviceId, TelemetryRequest request) {
        requireDevice(deviceId);
        String telemetryId = Ids.shortId("tel");
        String receivedAt = Instant.now().toString();
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("telemetryId", telemetryId);
        record.put("deviceId", deviceId);
        record.put("metricType", request.getMetricType());
        record.put("recordedAt", request.getRecordedAt() != null ? request.getRecordedAt() : receivedAt);
        record.put("values", request.getValues() != null ? request.getValues() : Map.of());
        record.put("accepted", true);
        record.put("receivedAt", receivedAt);
        stateStore.saveTelemetry(telemetryId, record);
        return record;
    }

    private Device requireDevice(String deviceId) {
        return stateStore.getDevice(deviceId)
                .orElseThrow(() -> new MmspException(404, "DEVICE_NOT_FOUND", "Device not found: " + deviceId));
    }
}
