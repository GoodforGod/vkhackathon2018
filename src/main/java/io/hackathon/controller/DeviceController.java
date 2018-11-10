package io.hackathon.controller;

import io.hackathon.manager.impl.NotifyHttpManager;
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
    private NotifyHttpManager notifyManager;

    @GetMapping("/device/alive/{id}")
    public String pingAlive(@PathVariable("id") String deviceId, HttpServletRequest request) {
        return notifyManager.getResponse(deviceId, request.getRemoteAddr());
    }
}
