package com.mmsp.service;

import com.mmsp.model.Device;
import com.mmsp.model.GlobalScenario;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StateStore {

    GlobalScenario setGlobalScenario(String scenario, int latencyMs);

    GlobalScenario getGlobalScenario();

    Device saveDevice(Device device);

    Optional<Device> getDevice(String deviceId);

    List<Device> listDevices();

    Optional<Device> updateDevice(String deviceId, DeviceUpdater updater);

    void saveCommand(String commandId, Map<String, Object> command);

    void saveTelemetry(String telemetryId, Map<String, Object> record);

    Map<String, Object> stats();

    boolean healthy();

    String backend();

    @FunctionalInterface
    interface DeviceUpdater {
        void apply(Device device);
    }
}
