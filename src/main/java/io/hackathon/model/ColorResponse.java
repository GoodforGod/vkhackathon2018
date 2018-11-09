package io.hackathon.model;

import java.util.Collections;
import java.util.List;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public class ColorResponse {

    private Color color;
    private boolean needRecalculation;
    private List<String> devices;

    private ColorResponse(Color color, boolean needRecalculation, List<String> devices) {
        this.color = color;
        this.needRecalculation = needRecalculation;
        this.devices = devices;
    }

    public static ColorResponse valid(Color color) {
        return new ColorResponse(color, false, Collections.emptyList());
    }

    public static ColorResponse recalculate(List<String> devices) {
        return new ColorResponse(null, false, devices);
    }

    public Color getColor() {
        return color;
    }

    public boolean isNeedRecalculation() {
        return needRecalculation;
    }

    public List<String> getDevices() {
        return devices;
    }
}
