package io.hackathon.model.dto;

import io.hackathon.model.Color;

import java.util.List;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 10.11.2018
 */
public class PathTO {

    private boolean isOptimal;
    private Color color;

    private String pathId;
    private int length;
    private List<String> devices;
    private String destDevice;

    public PathTO(boolean isOptimal, Color color, String pathId, int length, List<String> devices, String destDevice) {
        this.isOptimal = isOptimal;
        this.color = color;
        this.pathId = pathId;
        this.length = length;
        this.devices = devices;
        this.destDevice = destDevice;
    }

    public boolean isOptimal() {
        return isOptimal;
    }

    public Color getColor() {
        return color;
    }

    public String getPathId() {
        return pathId;
    }

    public int getLength() {
        return length;
    }

    public List<String> getDevices() {
        return devices;
    }

    public String getDestDevice() {
        return destDevice;
    }
}
