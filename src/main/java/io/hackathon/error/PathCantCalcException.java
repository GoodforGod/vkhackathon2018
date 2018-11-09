package io.hackathon.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PathCantCalcException extends RuntimeException {

    public PathCantCalcException() {
        super("Can't calculate path.");
    }
}
