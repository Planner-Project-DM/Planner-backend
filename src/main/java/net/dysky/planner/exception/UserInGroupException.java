package net.dysky.planner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "User is already in the group")
public class UserInGroupException extends RuntimeException {
    public UserInGroupException(String message) {
        super(message);
    }
}
