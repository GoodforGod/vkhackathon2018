package io.hackathon.manager.impl;

import io.hackathon.model.dao.Device;
import io.hackathon.storage.impl.DeviceStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@Service
public class DeviceManager {

    @Autowired
    private DeviceStorage storage;

    public boolean alive(String deviceId) {
        Optional<Device> device = storage.find(deviceId);
        device.ifPresent(d -> {
            d.markAsAlive();
            storage.save(d);
        });

        return device.isPresent();
    }

    @Scheduled(cron = "0 0 */1 * * *")
    public void resetAliveState() {
        //
    }
}
