package io.hackathon.model;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public enum Color {
    RED(""),
    ORANGE(""),
    YELLOW(""),
    GREEN(""),
    CYEN(""),
    BLUE(""),
    PURPLE("");

    private final String rgb;

    Color(String rgb) {
        this.rgb = rgb;
    }

    public String getRgb() {
        return rgb;
    }

    public static Set<Color> available(Set<Color> colors) {
        return Arrays.stream(Color.values())
                .filter(color -> !colors.contains(color))
                .collect(Collectors.toSet());
    }
}
