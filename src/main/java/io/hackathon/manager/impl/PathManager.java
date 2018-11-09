package io.hackathon.manager.impl;

import io.hackathon.error.PathException;
import io.hackathon.model.Path;
import io.hackathon.model.dao.Device;
import io.hackathon.storage.impl.DeviceStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@Service
public class PathManager {

    @Autowired
    private DeviceStorage storage;

    private final Comparator<Path> pathComparator = Comparator.comparingInt(p -> p.getDevices().size());

    public Path findPath(String startDeviceId, int zoneId, int roomId) {
        final Device startDevice = storage.find(startDeviceId).orElse(null);
        if (startDevice == null)
            throw new PathException("Start device id doesn't exist.");

        final List<Device> startDevices = storage.findByZoneAndRoom(startDevice.getZoneId(), startDevice.getRoomId());
        final Set<Device> destDevices = new HashSet<>(storage.findByZoneAndRoom(zoneId, roomId));
        if (destDevices.isEmpty())
            throw new PathException("Destination room doesn't exist.");

        final Map<Device, Path> paths = new HashMap<>();
        for (Device device : startDevices) {
            Path path = findShortestPath(device, destDevices);
            paths.put(device, path);
        }

        return paths.entrySet().stream()
                .filter(p -> !p.getValue().isEmpty())
                .map(Map.Entry::getValue)
                .min(pathComparator)
                .orElse(Path.EMPTY);
    }

    private Path findShortestPath(Device startDevice, Set<Device> destinationDevices) {
        final List<Device> all = storage.findAll();

        final Map<String, List<String>> shortPathMap = new HashMap<>();
        final Map<Device, Integer> edgyMap = all.stream()
                .collect(Collectors.toMap(e -> e, e -> Integer.MAX_VALUE));
        final Set<Device> settledDevices = new HashSet<>();
        final Set<Device> unsettledDevices = new HashSet<>();
        unsettledDevices.add(startDevice);
        edgyMap.put(startDevice, 0);

        while (!unsettledDevices.isEmpty()) {
            final Device device = getLowestDistanceNode(unsettledDevices, edgyMap);
            unsettledDevices.remove(device);
            for (Map.Entry<String, Integer> entry : device.getEdges().entrySet()) {
                storage.find(entry.getKey()).ifPresent(d -> {
                    if (!settledDevices.contains(d)) {
                        calcMinDistance(d, entry.getValue(), device, edgyMap, shortPathMap);
                        unsettledDevices.add(d);
                    }
                });
            }

            settledDevices.add(device);
            if (destinationDevices.contains(device))
                break;
        }

        final Map.Entry<String, List<String>> path = shortPathMap.entrySet().stream()
                .filter(e -> destinationDevices.stream().anyMatch(d -> e.getKey().equals(d.getId())))
                .findFirst()
                .orElse(null);

        if (path == null)
            return Path.EMPTY;

        final int pathLength = edgyMap.entrySet().stream()
                .filter(e -> e.getKey().getId().equals(path.getKey()))
                .map(Map.Entry::getValue)
                .findAny().orElse(0);

        return new Path(pathLength, path.getValue(), path.getKey());
    }

    private static Device getLowestDistanceNode(Set<Device> unsettledDevices,
                                                Map<Device, Integer> edgyMap) {
        return edgyMap.entrySet().stream()
                .filter(e -> unsettledDevices.contains(e.getKey()))
                // Filter for alive or other devs
                .min(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static void calcMinDistance(Device evaluationDevice,
                                        Integer edgeLegth,
                                        Device sourceDevice,
                                        Map<Device, Integer> edgyMap,
                                        Map<String, List<String>> shortPathMap) {
        int sourceDistance = edgyMap.get(sourceDevice);
        int evalDistance = edgyMap.get(evaluationDevice);
        int pathLengthSum = sourceDistance + edgeLegth;
        if (pathLengthSum < evalDistance) {
            edgyMap.put(evaluationDevice, pathLengthSum);
            final ArrayList<String> newPath = new ArrayList<>(shortPathMap.computeIfAbsent(sourceDevice.getId(),
                    (k) -> new ArrayList<>()));
            newPath.add(sourceDevice.getId());
            shortPathMap.put(evaluationDevice.getId(), newPath);
        }
    }


}
