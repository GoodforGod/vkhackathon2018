package io.hackathon.error;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public class PathException extends RuntimeException {

    public PathException() {
        super("Start device id doest exist.");
    }

    public PathException(String message) {
        super(message);
    }
}
