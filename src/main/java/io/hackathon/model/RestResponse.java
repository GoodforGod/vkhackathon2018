package io.hackathon.model;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 11.11.2018
 */
public class RestResponse<T> {

    private boolean isError;
    private String errorDetails;
    private T result;

    private RestResponse(boolean isError, String errorDetails, T result) {
        this.isError = isError;
        this.errorDetails = errorDetails;
        this.result = result;
    }

    public static <T> RestResponse valid(T t) {
        return new RestResponse<T>(false, null, t);
    }

    public static <T> ResponseEntity<RestResponse<T>> validEntity(T t) {
        return ResponseEntity.ok(new RestResponse<T>(false, null, t));
    }

    public static <T> RestResponse error(String details) {
        return new RestResponse<T>(true, details, null);
    }

    public static <T> ResponseEntity<RestResponse<T>> errorEntity(String details, HttpStatus status) {
        return new ResponseEntity<>(new RestResponse<T>(true, details, null), status);
    }

    public boolean isError() {
        return isError;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public T getResult() {
        return result;
    }
}
