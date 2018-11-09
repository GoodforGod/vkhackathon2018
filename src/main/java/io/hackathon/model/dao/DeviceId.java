package io.hackathon.model.dao;

import java.util.Objects;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public class DeviceId {

    private int zoneId;
    private int roomId;
    private int deviceId;

    public DeviceId() { }

    public DeviceId(int zoneId, int roomId, int deviceId) {
        this.zoneId = zoneId;
        this.roomId = roomId;
        this.deviceId = deviceId;
    }

    public String getFullId() {
        return zoneId + "_" + roomId + "_" + deviceId;
    }

    public int getZoneId() {
        return zoneId;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceId deviceId1 = (DeviceId) o;
        return zoneId == deviceId1.zoneId &&
                roomId == deviceId1.roomId &&
                deviceId == deviceId1.deviceId;
    }

    @Override
    public int hashCode() {

        return Objects.hash(zoneId, roomId, deviceId);
    }
}
