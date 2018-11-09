package io.hackathon.model.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@Document
public class Device {

    @Id
    private String id;

    @NotNull
    private DeviceId detailedId;

    private boolean isEdgy;
    private boolean isAlive;

    /**
     * DeviceId with which have edge -> path to that device
     */
    private Map<String, Integer> edges = new HashMap<>();

    private List<Cut> cuts = new ArrayList<>();

    public Device() { }

    public Device(int zoneId, int roomId, int deviceId) {
        this.detailedId = new DeviceId(zoneId, roomId, deviceId);
        this.id = this.detailedId.getFullId();
    }

    public static Device of(String fullId) {
        try {
            String[] split = fullId.split("_");
            int zoneId = Integer.valueOf(split[0]);
            int roomId = Integer.valueOf(split[1]);
            int deviceId = Integer.valueOf(split[2]);
            return new Device(zoneId, roomId, deviceId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public void addEdgy(String deviceId, int pathLength) {
        this.edges.put(deviceId, pathLength);
    }

    public boolean isSameZone(Device device) {
        return this.detailedId.getZoneId() == device.getDetailedId().getZoneId();
    }

    public boolean isSameRoom(Device device) {
        return isSameZone(device)
                && this.detailedId.getRoomId() == device.getDetailedId().getRoomId();
    }

    public DeviceId getDetailedId() {
        return detailedId;
    }

    public boolean isEdgy() {
        return isEdgy;
    }

    public void markAsEdgy() {
        this.isEdgy = true;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void markAsAlive() {
        this.isAlive = true;
    }

    public Map<String, Integer> getEdges() {
        return edges;
    }

    public List<Cut> getCuts() {
        return cuts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(detailedId, device.detailedId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(detailedId);
    }
}
