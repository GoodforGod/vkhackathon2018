package io.hackathon.storage.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hackathon.model.dao.Device;
import io.hackathon.model.dto.DevicePaths;
import io.hackathon.model.dto.MapGraph;
import io.hackathon.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@Service
public class DeviceStorage extends BasicCacheStorage<Device, String> {

    private final DeviceRepository deviceRepository;

    @Value("${map.path:C:\\Users\\GoodforGod\\IdeaProjects\\hermitage\\src\\resources\\map_small.json}")
    private String defaultMapPath;

    @Autowired
    public DeviceStorage(DeviceRepository repository) {
        super(repository);
        this.deviceRepository = repository;
    }

    public List<Device> findByIds(List<String> deviceIds) {
        return deviceIds.stream()
                .map(this::find)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<Device> findByZone(int zoneId) {
        return deviceRepository.findByDetailedIdZoneId(zoneId);
    }

    public List<Device> findByZoneAndRoom(int zoneId, int roomId) {
        return deviceRepository.findByDetailedIdZoneIdAndDetailedIdRoomId(zoneId, roomId);
    }

    public List<String> loadDefaultMap() {
        try {
            final File file = new File(defaultMapPath);
            final MapGraph marked = new ObjectMapper().readValue(file, MapGraph.class);
            deleteAll();
            return loadMap(marked);
        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage());
            return Collections.emptyList();
        }
    }

    public List<String> loadMap(MapGraph map) {
        if(map == null || CollectionUtils.isEmpty(map.getMap()))
            return Collections.emptyList();

        final List<String> invalidIds = new ArrayList<>();

        for (Map.Entry<String, List<DevicePaths>> entry : map.getMap().entrySet()) {
            final Device device = find(entry.getKey()).orElse(Device.of(entry.getKey()));
            if(device == null) {
                invalidIds.add(entry.getKey());
                continue;
            }

            for (DevicePaths paths : entry.getValue()) {
                Device devEdgy = Device.of(paths.getDeviceId());
                if(devEdgy == null) {
                    invalidIds.add(entry.getKey());
                    continue;
                }

                device.addEdgy(paths.getDeviceId(), paths.getPath());
                if(device.isSameZone(devEdgy)) {
                    device.markAsEdgy();
                }
            }

            save(device);
        }

        return invalidIds;
    }
}
