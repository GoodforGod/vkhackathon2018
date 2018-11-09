package io.hackathon.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class MapLoadException extends RuntimeException {

    public MapLoadException(List<String> ids) {
        super("Can't load map, invalid ids found : " + ids.toString());
    }
}
