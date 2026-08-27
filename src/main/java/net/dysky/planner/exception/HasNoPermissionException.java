package net.dysky.planner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "User does not have permission to perform this action")
public class HasNoPermissionException extends RuntimeException {
    public HasNoPermissionException(String message) {
        super(message);
    }
}
