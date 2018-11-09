package io.hackathon.model.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    private DeviceId detailedId;

    private boolean isEdgy;
    private boolean isAlive;

    private Map<String, Integer> edges = new HashMap<>();

    private List<Cut> cuts = new ArrayList<>();

    public Device(String id, boolean isEdgy) {
        this.id = id;
        String[] split = id.split("_");
        this.detailedId = new DeviceId(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
        this.isEdgy = isEdgy;
    }

    public String getId() {
        return id;
    }

    public DeviceId getDetailedId() {
        return detailedId;
    }

    public boolean isEdgy() {
        return isEdgy;
    }

    public boolean isAlive() {
        return isAlive;
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
