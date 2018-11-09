package io.hackathon.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class MapPicException extends RuntimeException {

    public MapPicException() {
        super("can't load map image");
    }
}
