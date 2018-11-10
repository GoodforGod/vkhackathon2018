package io.hackathon.manager.impl;

import io.hackathon.model.Color;
import io.hackathon.model.ColorBox;
import io.hackathon.model.ColorResponse;
import io.hackathon.model.Path;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@Service
public class ColorManager {

    private final Map<String, ColorBox> colorBoxMap = new ConcurrentHashMap<>();

    public ColorResponse assign(final Path path) {
        final Set<String> devices = new HashSet<>(path.getDevices());
        final Color color = colorBoxMap.entrySet().stream()
                .filter(e -> e.getValue().containsPath(path.getPathId()))
                .findFirst()
                .map(e -> e.getValue().getColor(path.getPathId()))
                .orElse(null);

        if(color != null)
            return ColorResponse.valid(color);

        final Set<Color> colorsOccupied = colorBoxMap.entrySet().stream()
                .filter(e -> devices.contains(e.getKey()))
                .map(e -> e.getValue().getColors())
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        final Set<Color> available = Color.available(colorsOccupied);
        if (available.isEmpty()) {
            // Devices with full color specter occupied
            final List<String> fullColorBoxDevices = colorBoxMap.entrySet().stream()
                    .filter(e -> devices.contains(e.getKey()))
                    .filter(e -> e.getValue().getColors().size() == Color.values().length)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            return ColorResponse.recalculate(fullColorBoxDevices);
        }

        final Color availableColor = available.stream().findAny().get();
        for (String device : devices) {
            ColorBox box = colorBoxMap.computeIfAbsent(device, (k) -> new ColorBox());
            box.addColor(path.getPathId(), availableColor);
            colorBoxMap.put(device, box);
        }

        return ColorResponse.valid(availableColor);
    }

    public void reset(String pathId, Set<String> devices) {
        this.colorBoxMap.entrySet().stream()
                .filter(e -> devices.contains(e.getKey()))
                .forEach(e -> e.getValue().reset(pathId));
    }
}