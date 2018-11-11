package io.hackathon.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 11.11.2018
 */
public class PathBox {

    private Path path;
    private LocalDateTime timestamp;

    public PathBox(Path path) {
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public Path getPath() {
        return path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathBox pathBox = (PathBox) o;
        return Objects.equals(path, pathBox.path);
    }

    @Override
    public int hashCode() {

        return Objects.hash(path);
    }
}
