package io.hackathon.storage.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hackathon.model.dao.Device;
import io.hackathon.model.dto.DevicePaths;
import io.hackathon.model.dto.MapGraph;
import io.hackathon.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@Service
public class DeviceStorage extends BasicCacheStorage<Device, String> {

    @Autowired
    public DeviceStorage(DeviceRepository repository) {
        super(repository);
    }

    public List<String> loadDefaultMap() {
        try {
            final ClassLoader classLoader = getClass().getClassLoader();
            final File file = new File(Objects.requireNonNull(classLoader.getResource("map_small.json")).getFile());
            final MapGraph marked = new ObjectMapper().readValue(file, MapGraph.class);
            deleteAll();
            return loadMap(marked);
        } catch (IOException e) {
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
