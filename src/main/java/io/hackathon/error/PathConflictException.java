package io.hackathon.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 11.11.2018
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class PathConflictException extends RuntimeException {

    public PathConflictException() {
        super("Path collision detected");
    }
}
