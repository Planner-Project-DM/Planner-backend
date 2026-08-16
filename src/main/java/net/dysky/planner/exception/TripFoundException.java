package net.dysky.planner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Trip already exists")
public class TripFoundException extends RuntimeException {
    public TripFoundException(String message) {
        super(message);
    }
}
