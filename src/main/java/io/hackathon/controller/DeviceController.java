package io.hackathon.controller;

import io.hackathon.manager.impl.DeviceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@RestController
public class DeviceController {

    @Autowired
    private DeviceManager deviceManager;

    @GetMapping("/device/alive/{id}")
    public boolean pingAlive(@PathVariable("id") String deviceId) {
        return deviceManager.alive(deviceId);
    }
}
