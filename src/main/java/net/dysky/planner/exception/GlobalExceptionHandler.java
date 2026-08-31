package net.dysky.planner.exception;

import jakarta.servlet.http.HttpServletRequest;
import net.dysky.planner.response.ResponseDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserExistException.class)
    public ResponseEntity<ResponseDTO> handleUserExist(UserExistException ex, HttpServletRequest request) {

        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleUserNotFoundExist(UserNotFoundException ex, HttpServletRequest request) {

        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TripNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleTripNotFound(TripNotFoundException ex, HttpServletRequest request)  {
        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FriendshipNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleFriendshipNotFound(FriendshipNotFoundException ex, HttpServletRequest request) {
        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FriendshipExistsException.class)
    public ResponseEntity<ResponseDTO> handleFriendshipNotFound(FriendshipExistsException ex, HttpServletRequest request) {
        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "Data integrity violation occurred.";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (ex.getMessage() != null && ex.getMessage().contains("uk_trip_name_and_creator")) {
            message = "You already have a trip with this name. Please choose a different name.";
            status = HttpStatus.CONFLICT;
        }

        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                status.value(),
                message,
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(BudgetNotPositiveException.class)
    public ResponseEntity<ResponseDTO> handleBudgetNotPositive(BudgetNotPositiveException ex, HttpServletRequest request) {
        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TripFoundException.class)
    public ResponseEntity<ResponseDTO> handleTripFound(TripFoundException ex, HttpServletRequest request) {
        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserInGroupException.class)
    public ResponseEntity<ResponseDTO> handleUserInGroup(UserInGroupException ex, HttpServletRequest request) {
        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BalanceOverBudgetException.class)
    public ResponseEntity<ResponseDTO> handleBalanceOverBudget(BalanceOverBudgetException ex, HttpServletRequest request) {
        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HasNoPermissionException.class)
    public ResponseEntity<ResponseDTO> handleHasNoPermission(HasNoPermissionException ex, HttpServletRequest request) {
        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseDTO> handleAccessDeniedException( AccessDeniedException ex, HttpServletRequest request) {

        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Access Denied: " + ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDTO> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleUnexpectedError(Exception ex, HttpServletRequest request) {

        ResponseDTO error = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage().isBlank() ? "Internal Server Error" : ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
