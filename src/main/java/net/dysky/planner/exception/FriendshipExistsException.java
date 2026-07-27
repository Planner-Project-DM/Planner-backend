package net.dysky.planner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Friendship already exists")
public class FriendshipExistsException extends RuntimeException {
    public FriendshipExistsException(String message) {
        super(message);
    }
}
