package io.hackathon.model.dao;

import java.util.List;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public class Cut {

    private List<String> devices;
    private int pathLength;

    public Cut(List<String> devices, int pathLength) {
        this.devices = devices;
        this.pathLength = pathLength;
    }

    public List<String> getDevices() {
        return devices;
    }

    public int getPathLength() {
        return pathLength;
    }
}
