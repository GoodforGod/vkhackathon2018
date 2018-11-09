package io.hackathon.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public class ColorBox {

    private Map<String, Color> colorMap = new HashMap<>();

    public ColorBox addColor(String pathId, Color color) {
        colorMap.put(pathId, color);
        return this;
    }

    public ColorBox reset(String pathId) {
        colorMap.remove(pathId);
        return this;
    }

    public Color getColor(String pathId) {
        return colorMap.get(pathId);
    }

    public boolean containsPath(String pathId) {
        return colorMap.containsKey(pathId);
    }

    public Set<Color> getColors() {
        return colorMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }
}
