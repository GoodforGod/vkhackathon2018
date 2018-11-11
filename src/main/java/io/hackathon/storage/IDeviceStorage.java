package io.hackathon.storage;

import io.hackathon.model.dao.Device;
import io.hackathon.model.dto.MapGraph;

import java.util.List;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public interface IDeviceStorage {

    List<Device> findByIds(List<String> deviceIds);

    List<Device> findByZone(int zoneId);

    List<Device> findByZoneAndRoom(int zoneId, int roomId);

    List<String> loadDefaultMap();
    List<String> loadMap(MapGraph map);
}
