package com.mmsp.controller;

import com.mmsp.agents.EnhancementAgent;
import com.mmsp.agents.StabilityAgent;
import com.mmsp.dto.CommandRequest;
import com.mmsp.dto.DeviceRegisterRequest;
import com.mmsp.dto.DeviceStatusRequest;
import com.mmsp.dto.HeartbeatRequest;
import com.mmsp.dto.ScenarioSetRequest;
import com.mmsp.dto.TelemetryRequest;
import com.mmsp.exception.MmspException;
import com.mmsp.model.Device;
import com.mmsp.model.ScenarioApplicationResult;
import com.mmsp.service.DeviceSimulator;
import com.mmsp.service.ScenarioManager;
import com.mmsp.service.StateStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Devices")
public class DeviceController {

    private final DeviceSimulator deviceSimulator;
    private final ScenarioManager scenarioManager;
    private final StateStore stateStore;
    private final EnhancementAgent enhancementAgent;
    private final StabilityAgent stabilityAgent;

    public DeviceController(
            DeviceSimulator deviceSimulator,
            ScenarioManager scenarioManager,
            StateStore stateStore,
            EnhancementAgent enhancementAgent,
            StabilityAgent stabilityAgent
    ) {
        this.deviceSimulator = deviceSimulator;
        this.scenarioManager = scenarioManager;
        this.stateStore = stateStore;
        this.enhancementAgent = enhancementAgent;
        this.stabilityAgent = stabilityAgent;
    }

    @GetMapping("/devices")
    @Operation(summary = "List simulated devices")
    public Map<String, Object> listDevices() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("count", stateStore.listDevices().size());
        body.put("devices", stateStore.listDevices());
        return body;
    }

    @GetMapping("/devices/{deviceId}")
    @Operation(summary = "Get device by id")
    public Map<String, Object> getDevice(@PathVariable String deviceId) {
        Device device = stateStore.getDevice(deviceId)
                .orElseThrow(() -> new MmspException(404, "DEVICE_NOT_FOUND", "Device not found: " + deviceId));
        return Map.of("device", device);
    }

    @PostMapping("/devices/register")
    @Operation(summary = "Register a simulated device")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody DeviceRegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        return withScenario(httpRequest, null, resolved -> {
            Device device = deviceSimulator.register(request);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("deviceId", device.getDeviceId());
            body.put("deviceSerial", device.getDeviceSerial());
            body.put("status", device.getStatus());
            body.put("state", device.getState());
            body.put("registeredAt", device.getRegisteredAt());
            body.put("scenario", resolved.getKey());
            body.put("scenarioSource", resolved.getSource());
            return ResponseEntity.status(201).body(body);
        });
    }

    @PutMapping("/devices/{deviceId}/status")
    @Operation(summary = "Update device status / state")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable String deviceId,
            @Valid @RequestBody DeviceStatusRequest request,
            HttpServletRequest httpRequest
    ) {
        Device device = requireDevice(deviceId);
        return withScenario(httpRequest, device, resolved -> {
            Map<String, Object> result = deviceSimulator.updateStatus(deviceId, request.getState(), request.getReason());
            result.put("scenario", resolved.getKey());
            result.put("scenarioSource", resolved.getSource());
            return ResponseEntity.ok(result);
        });
    }

    @PostMapping("/devices/{deviceId}/heartbeat")
    @Operation(summary = "Device heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(
            @PathVariable String deviceId,
            @RequestBody(required = false) HeartbeatRequest request,
            HttpServletRequest httpRequest
    ) {
        Device device = requireDevice(deviceId);
        return withScenario(httpRequest, device, resolved -> {
            Device updated = deviceSimulator.heartbeat(deviceId, request != null ? request : new HeartbeatRequest());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("deviceId", updated.getDeviceId());
            body.put("ack", true);
            body.put("serverTime", Instant.now().toString());
            body.put("state", updated.getState());
            body.put("scenario", resolved.getKey());
            body.put("scenarioSource", resolved.getSource());
            return ResponseEntity.ok(body);
        });
    }

    @PostMapping("/devices/{deviceId}/commands")
    @Operation(summary = "Execute / accept a device command")
    public ResponseEntity<Map<String, Object>> command(
            @PathVariable String deviceId,
            @Valid @RequestBody CommandRequest request,
            HttpServletRequest httpRequest
    ) {
        Device device = requireDevice(deviceId);
        return withScenario(httpRequest, device, resolved -> {
            Map<String, Object> command = deviceSimulator.acceptCommand(deviceId, request);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("commandId", command.get("commandId"));
            body.put("deviceId", command.get("deviceId"));
            body.put("commandType", command.get("commandType"));
            body.put("status", command.get("status"));
            body.put("correlationId", command.get("correlationId"));
            body.put("acceptedAt", command.get("acceptedAt"));
            body.put("scenario", resolved.getKey());
            body.put("scenarioSource", resolved.getSource());
            return ResponseEntity.accepted().body(body);
        });
    }

    @PostMapping("/devices/{deviceId}/telemetry")
    @Operation(summary = "Ingest device telemetry")
    public ResponseEntity<Map<String, Object>> telemetry(
            @PathVariable String deviceId,
            @Valid @RequestBody TelemetryRequest request,
            HttpServletRequest httpRequest
    ) {
        Device device = requireDevice(deviceId);
        return withScenario(httpRequest, device, resolved -> {
            Map<String, Object> record = deviceSimulator.acceptTelemetry(deviceId, request);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("telemetryId", record.get("telemetryId"));
            body.put("deviceId", record.get("deviceId"));
            body.put("accepted", record.get("accepted"));
            body.put("receivedAt", record.get("receivedAt"));
            body.put("scenario", resolved.getKey());
            body.put("scenarioSource", resolved.getSource());
            return ResponseEntity.accepted().body(body);
        });
    }

    @PutMapping("/devices/{deviceId}/scenario")
    @Operation(summary = "Set per-device scenario override")
    public Map<String, Object> setDeviceScenario(
            @PathVariable String deviceId,
            @Valid @RequestBody ScenarioSetRequest request
    ) {
        Device device = scenarioManager.setDeviceScenario(deviceId, request.getScenario(), request.getLatencyMs());
        stabilityAgent.onScenarioChange(Map.of(
                "scope", "device",
                "deviceId", device.getDeviceId(),
                "scenario", device.getScenario()
        ));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deviceId", device.getDeviceId());
        body.put("scenario", device.getScenario());
        body.put("scenarioLatencyMs", device.getScenarioLatencyMs());
        return body;
    }

    private Device requireDevice(String deviceId) {
        return stateStore.getDevice(deviceId)
                .orElseThrow(() -> new MmspException(404, "DEVICE_NOT_FOUND", "Device not found: " + deviceId));
    }

    private ResponseEntity<Map<String, Object>> withScenario(
            HttpServletRequest request,
            Device device,
            Function<com.mmsp.model.ResolvedScenario, ResponseEntity<Map<String, Object>>> handler
    ) {
        ScenarioApplicationResult applied = scenarioManager.apply(request, device);
        enhancementAgent.onApiInvocation(Map.of(
                "path", request.getRequestURI(),
                "method", request.getMethod(),
                "scenario", applied.getResolved().getKey(),
                "source", applied.getResolved().getSource()
        ));

        if (applied.isShortCircuit()) {
            if (applied.getStatusCode() >= 500) {
                stabilityAgent.onAnomaly(Map.of(
                        "type", "scenario_fault",
                        "scenario", applied.getResolved().getKey(),
                        "statusCode", applied.getStatusCode(),
                        "path", request.getRequestURI()
                ));
            }
            return ResponseEntity.status(applied.getStatusCode()).body(applied.getBody());
        }
        return handler.apply(applied.getResolved());
    }
}
