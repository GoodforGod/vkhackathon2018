package io.hackathon.controller;

import io.hackathon.manager.impl.DeviceManager;
import io.hackathon.storage.impl.DeviceStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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

    @Autowired
    private DeviceStorage deviceStorage;

    @GetMapping("/device/alive/{id}")
    public boolean pingAlive(@PathVariable("id") String deviceId, HttpServletRequest request) {
        boolean alive = deviceManager.alive(deviceId);
        if(alive) {
            deviceStorage.find(deviceId).ifPresent(d -> {
                d.rememberIp(request.getRemoteAddr());
                deviceStorage.save(d);
            });
        }

        return alive;
    }
}
