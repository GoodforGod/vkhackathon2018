package io.hackathon.manager.impl;

import io.hackathon.manager.INotifyManager;
import io.hackathon.model.Color;
import io.hackathon.model.dao.Device;
import io.hackathon.service.impl.NotifyUdpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 10.11.2018
 */
@Service
public class NotifyHttpManager implements INotifyManager {

    @Autowired
    private DeviceManager deviceManager;

    private final Map<String, Queue<String>> responseMap = new ConcurrentHashMap<>();

    public String getResponse(String deviceId, String ip, int port) {
        final Queue<String> queue = responseMap.get(deviceId);
        if(queue == null || queue.isEmpty())
            return null;

        deviceManager.alive(deviceId, ip, port);
        return queue.poll();
    }

    @Override
    public void notifyWithColor(List<Device> devices,
                                String pathId,
                                Color color) {
        final String cmd = NotifyUdpService.Command.buildHigh(pathId, color);
        for (Device device : devices) {
            Queue<String> queue = responseMap.computeIfAbsent(device.getId(), (k) -> new ArrayDeque<>());
            queue.add(cmd);
            responseMap.put(device.getId(), queue);
        }
    }

    @Override
    public void notifyColorOff(List<Device> devices,
                               String pathId) {
        final String cmd = NotifyUdpService.Command.buildLow(pathId);
        for (Device device : devices) {
            Queue<String> queue = responseMap.computeIfAbsent(device.getId(), (k) -> new ArrayDeque<>());
            queue.add(cmd);
            responseMap.put(device.getId(), queue);
        }
    }
}
