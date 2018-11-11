package io.hackathon.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class PathException extends RuntimeException {

    public PathException(String message) {
        super(message);
    }
}
