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
    RED("1 0 0"),
    GREEN("0 1 0"),
    BLUE("0 0 1"),
    PURPLE("1 0 1"),
    YELLOW("1 1 0"),
    CYAN("0 1 1");

    private final String rgb;

    Color(String rgb) {
        this.rgb = rgb;
    }

    public String asRgb() {
        return rgb;
    }

    public static Set<Color> available(Set<Color> colors) {
        return Arrays.stream(Color.values())
                .filter(color -> !colors.contains(color))
                .collect(Collectors.toSet());
    }
}
