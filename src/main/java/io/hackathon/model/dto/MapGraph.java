package io.hackathon.model.dto;

import java.util.List;
import java.util.Map;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public class MapGraph {

    /**
     * DeviceId -> Paths
     */
    private Map<String, List<DevicePaths>> map;

    public Map<String, List<DevicePaths>> getMap() {
        return map;
    }
}
