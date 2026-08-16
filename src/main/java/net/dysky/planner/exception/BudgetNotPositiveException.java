package net.dysky.planner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Budget must be a positive number")
public class BudgetNotPositiveException extends RuntimeException {
    public BudgetNotPositiveException(String message) {
        super(message);
    }
}
