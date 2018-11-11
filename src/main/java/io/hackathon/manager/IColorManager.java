package io.hackathon.manager;

import io.hackathon.model.ColorResponse;
import io.hackathon.model.Path;

import java.util.Set;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 10.11.2018
 */
public interface IColorManager {

    ColorResponse assign(final Path path);

    void reset(String pathId, Set<String> devices);
}
