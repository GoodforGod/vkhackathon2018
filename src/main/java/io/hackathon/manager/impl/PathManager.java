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

    private final Map<String, Path> memorizedPaths = new HashMap<>();

    private final Comparator<Path> pathComparator = Comparator.comparingInt(p -> p.getDevices().size());

    public Path findPath(String startDeviceId, int zoneId, int roomId) {
        return findPath(startDeviceId, zoneId, roomId, Collections.emptySet());
    }

    public Path findPath(String startDeviceId, int zoneId, int roomId, Set<String> exclude) {
        final Device startDevice = storage.find(startDeviceId).orElse(null);
        if (startDevice == null)
            throw new PathException("Start device id doesn't exist.");

        final List<Device> startDevices = storage.findByZoneAndRoom(startDevice.getZoneId(), startDevice.getRoomId());
        final Set<Device> destDevices = new HashSet<>(storage.findByZoneAndRoom(zoneId, roomId));
        if (destDevices.isEmpty())
            throw new PathException("Destination room doesn't exist.");

        final Path memorizedPath = startDevices.stream()
                .map(startDev -> destDevices.stream()
                        .map(destDev -> Path.calcFirstId(startDev.getId(), destDev.getId()))
                        .collect(Collectors.toList()))
                .flatMap(List::stream)
                .map(id -> memorizedPaths.entrySet().stream()
                        .filter(e -> e.getKey().startsWith(id))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList()))
                .flatMap(List::stream)
                .filter(e -> !e.isEmpty())
                .findAny()
                .orElse(null);

        // Path already memorized
        if (memorizedPath != null && exclude.isEmpty())
            return memorizedPath;

        final Map<Device, Path> paths = new HashMap<>();
        for (Device device : startDevices) {
            final Path path = findShortestPath(device, destDevices, exclude);
            paths.put(device, path);
        }

        final Path path = paths.entrySet().stream()
                .filter(p -> !p.getValue().isEmpty())
                .map(Map.Entry::getValue)
                .min(pathComparator)
                .orElse(Path.EMPTY);

        if (!path.isEmpty())
            this.memorizedPaths.put(path.getPathId(), path);

        return path;
    }

    private Path findShortestPath(Device startDevice,
                                  Set<Device> destinationDevices,
                                  Set<String> exclude) {
        final List<Device> all = storage.findAll();

        final Map<String, List<String>> shortPathMap = new HashMap<>();
        final Map<Device, Integer> edgyMap = all.stream()
                .collect(Collectors.toMap(e -> e, e -> Integer.MAX_VALUE));
        final Set<Device> settledDevices = new HashSet<>();
        final Set<Device> unsettledDevices = new HashSet<>();
        unsettledDevices.add(startDevice);
        edgyMap.put(startDevice, 0);

        while (!unsettledDevices.isEmpty()) {
            final Device device = getLowestDistanceNode(unsettledDevices, edgyMap, exclude);
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
                                                Map<Device, Integer> edgyMap,
                                                Set<String> exclude) {
        return edgyMap.entrySet().stream()
                .filter(e -> unsettledDevices.contains(e.getKey()))
                .filter(e -> !exclude.contains(e.getKey().getId()))
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
