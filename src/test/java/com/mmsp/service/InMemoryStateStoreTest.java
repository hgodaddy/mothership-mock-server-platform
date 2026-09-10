package com.mmsp.service;

import com.mmsp.model.Device;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryStateStoreTest {

    @Test
    void tracksDevicesCommandsAndTelemetry() {
        InMemoryStateStore store = new InMemoryStateStore();
        Device device = new Device();
        device.setDeviceId("dev_test");
        device.setState("online");
        store.saveDevice(device);
        store.saveCommand("cmd_1", Map.of("status", "accepted"));
        store.saveTelemetry("tel_1", Map.of("accepted", true));
        store.setGlobalScenario("timeout", 200);

        assertEquals("timeout", store.getGlobalScenario().getScenario());
        assertEquals(1, store.listDevices().size());
        assertEquals("memory", store.backend());
        assertTrue(store.healthy());
        assertEquals(1, store.stats().get("devices"));
        assertEquals(1, store.stats().get("commands"));
        assertEquals(1, store.stats().get("telemetry"));
    }
}
