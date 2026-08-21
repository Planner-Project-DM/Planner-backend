package net.dysky.planner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Balance exceeds budget")
public class BalanceOverBudgetException extends RuntimeException {
    public BalanceOverBudgetException(String message) {
        super(message);
    }
}
