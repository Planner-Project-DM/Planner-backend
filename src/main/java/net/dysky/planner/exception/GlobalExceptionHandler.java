package net.dysky.planner.exception;

import jakarta.servlet.http.HttpServletRequest;
import net.dysky.planner.response.ResponseDTO;
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
