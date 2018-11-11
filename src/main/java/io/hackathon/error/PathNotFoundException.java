package io.hackathon.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 11.11.2018
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PathNotFoundException extends RuntimeException{

    public PathNotFoundException() {
        super("Path was not found");
    }
}
