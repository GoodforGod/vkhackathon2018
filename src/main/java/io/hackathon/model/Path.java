package io.hackathon.model;

import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public class Path {

    public static Path EMPTY = new Path(0, Collections.emptyList(), "0_0_0");

    private String pathId;
    private int length;
    private List<String> devices;
    private String destDevice;

    public Path(int length, List<String> devices, String destDevice) {
        this.pathId = (CollectionUtils.isEmpty(devices)) ? "" : calcId(devices, destDevice);
        this.length = length;
        this.devices = (CollectionUtils.isEmpty(devices)) ? Collections.emptyList() : devices;
        this.destDevice = destDevice;
    }

    public static String calcFirstId(String startDev, String dest) {
        String[] splitStart = startDev.split("_");
        String[] splitDest = dest.split("_");
        return splitStart[0] + splitStart[1] + "_" + splitDest[0] + splitDest[1];
    }

    private static String calcId(List<String> devices, String dest) {
        String firstIdPart = calcFirstId(devices.get(0), dest);
        int pathHash = devices.stream().collect(Collectors.joining()).hashCode();
        return firstIdPart + "_" + pathHash;
    }

    public boolean isEmpty() {
        return devices.isEmpty();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return Objects.equals(pathId, path.pathId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(pathId);
    }
}
