package io.hackathon.manager.impl;

import io.hackathon.model.dao.Device;
import io.hackathon.storage.impl.DeviceStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final Logger logger = LoggerFactory.getLogger(DeviceManager.class);

    @Autowired
    private DeviceStorage storage;

    public boolean dead(String deviceId) {
        Optional<Device> device = storage.find(deviceId);
        device.ifPresent(d -> {
            d.maskAsDead();
            logger.warn("DEVICE IS DEAD - " + d.getId());
            storage.save(d);
        });

        return device.isPresent();
    }

    public boolean alive(String deviceId, String ipAddress, int port) {
        Optional<Device> device = storage.find(deviceId);
        device.ifPresent(d -> {
            d.markAsAlive();
            d.rememberIp(ipAddress, port);
            logger.warn("DEVICE IS ALIVE - " + d.getId() + ", WITH IP - " + ipAddress + ", WITH PORT - " + port);
            storage.save(d);
        });

        if (!device.isPresent())
            logger.warn("DEVICE NOT FOUND, CANT MARK ALIVE - " + deviceId + ", WITH IP - " + ipAddress);

        return device.isPresent();
    }
}
