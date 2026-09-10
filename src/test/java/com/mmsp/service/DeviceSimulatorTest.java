package com.mmsp.service;

import com.mmsp.config.MmspProperties;
import com.mmsp.dto.CommandRequest;
import com.mmsp.dto.DeviceRegisterRequest;
import com.mmsp.dto.TelemetryRequest;
import com.mmsp.exception.MmspException;
import com.mmsp.model.Device;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceSimulatorTest {

    private DeviceSimulator deviceSimulator;

    @BeforeEach
    void setUp() {
        MmspProperties properties = new MmspProperties();
        StateStore stateStore = new InMemoryStateStore();
        deviceSimulator = new DeviceSimulator(properties, stateStore);
    }

    @Test
    void registersDeviceAndAcceptsCommandAndTelemetry() {
        DeviceRegisterRequest registerRequest = new DeviceRegisterRequest();
        registerRequest.setDeviceSerial("UNIT-1");
        registerRequest.setDeviceType("PAYMENT_TERMINAL");
        registerRequest.setFirmwareVersion("1.0.0");
        registerRequest.setSiteId("S1");

        Device device = deviceSimulator.register(registerRequest);
        assertTrue(device.getDeviceId().startsWith("dev_"));
        assertEquals("online", device.getState());

        CommandRequest commandRequest = new CommandRequest();
        commandRequest.setCommandType("REBOOT");
        Map<String, Object> command = deviceSimulator.acceptCommand(device.getDeviceId(), commandRequest);
        assertEquals("accepted", command.get("status"));

        TelemetryRequest telemetryRequest = new TelemetryRequest();
        telemetryRequest.setMetricType("TRANSACTION_STATS");
        telemetryRequest.setValues(Map.of("approvedCount", 1));
        Map<String, Object> telemetry = deviceSimulator.acceptTelemetry(device.getDeviceId(), telemetryRequest);
        assertEquals(true, telemetry.get("accepted"));
    }

    @Test
    void rejectsInvalidDeviceState() {
        DeviceRegisterRequest registerRequest = new DeviceRegisterRequest();
        registerRequest.setDeviceSerial("UNIT-2");
        registerRequest.setDeviceType("PAYMENT_TERMINAL");
        Device device = deviceSimulator.register(registerRequest);

        MmspException ex = assertThrows(
                MmspException.class,
                () -> deviceSimulator.updateStatus(device.getDeviceId(), "flying", null)
        );
        assertEquals("INVALID_STATE", ex.getCode());
    }
}
