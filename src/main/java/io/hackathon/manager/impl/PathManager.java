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

    public Path findPath(String startDeviceId, int zoneId, int roomId) {
        Optional<Device> device = storage.find(startDeviceId);
        if (!device.isPresent())
            throw new PathException("Start device id doest exist.");

        return null;
    }

    private Path findShortestPath(Device startDevice, Set<Device> destinationDevices) {
        final List<Device> all = storage.findAll();


        final Map<Device, List<String>> shortPathMap = new HashMap<>();
        final Map<Device, Integer> edgyMap = all.stream()
                .collect(Collectors.toMap(e -> e, e -> Integer.MAX_VALUE));
        final Set<Device> settledNodes = new HashSet<>();
        final Set<Device> unsettledNodes = new HashSet<>();
        unsettledNodes.add(startDevice);

        while (!unsettledNodes.isEmpty()) {
            final Device device = getLowestDistanceNode(unsettledNodes, edgyMap);
            unsettledNodes.remove(device);
            for (Map.Entry<String, Integer> entry : device.getEdges().entrySet()) {
                storage.find(entry.getKey()).ifPresent(d -> {
                    if (!settledNodes.contains(d)) {
                        calcMinDistance(d, entry.getValue(), device, edgyMap, shortPathMap);
                        unsettledNodes.add(d);
                    }
                });
            }
            settledNodes.add(device);
            if (destinationDevices.contains(device))
                break;
        }

        return null;
    }

    private static Device getLowestDistanceNode(Set<Device> unsettledNodes,
                                                Map<Device, Integer> edgyMap) {


        Device lowestDistanceNode = null;
        int lowestDistance = Integer.MAX_VALUE;
        for (Device device : unsettledNodes) {
            int nodeDistance = edgyMap.get(device);
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = device;
            }
        }
        return lowestDistanceNode;
    }

    private static void calcMinDistance(Device evaluationDevice,
                                        Integer edgeWeigh,
                                        Device sourceDevice,
                                        Map<Device, Integer> edgyMap,
                                        Map<Device, List<String>> shortPathMap) {
        int sourceDistance = edgyMap.get(sourceDevice);
        int evalDistance = edgyMap.get(evaluationDevice);
        int pathLengthSum = sourceDistance + edgeWeigh;
        if (pathLengthSum < evalDistance) {
            edgyMap.put(sourceDevice, pathLengthSum);
            ArrayList<String> newPath = new ArrayList<>(shortPathMap.computeIfAbsent(sourceDevice, (k) -> new ArrayList<>()));
            newPath.add(sourceDevice.getId());
            shortPathMap.put(evaluationDevice, newPath);
        }
    }


}
