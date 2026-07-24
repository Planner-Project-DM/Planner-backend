package net.dysky.planner.exception;

import jakarta.servlet.http.HttpServletRequest;
import net.dysky.planner.response.ResponseDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleUnexpectedError(Exception ex, HttpServletRequest request) {

        ResponseDTO error = new ResponseDTO(
              LocalDateTime.now(),
              HttpStatus.INTERNAL_SERVER_ERROR.value(),
              "Internal Server Error",
              request.getRequestURI(),
              null
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
