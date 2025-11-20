package br.com.gabrielcaio.verso.controllers.handler;

import br.com.gabrielcaio.verso.controllers.error.BusinessException;
import br.com.gabrielcaio.verso.controllers.error.DataBaseException;
import br.com.gabrielcaio.verso.controllers.error.EntityExistsException;
import br.com.gabrielcaio.verso.controllers.error.ErrorMessage;
import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.controllers.error.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExeceptionHandler
{

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessage> handlerResourceNotFound(
            ResourceNotFoundException e, HttpServletRequest request
    )
    {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorMessage err = new ErrorMessage(
                Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status)
                .body(err);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorMessage> handlerEntityExists(
            EntityExistsException e, HttpServletRequest request
    )
    {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorMessage err = new ErrorMessage(
                Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status)
                .body(err);
    }

    @ExceptionHandler(DataBaseException.class)
    public ResponseEntity<ErrorMessage> handlerDataBase(
            DataBaseException e, HttpServletRequest request
    )
    {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorMessage err = new ErrorMessage(
                Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status)
                .body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handlerMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpServletRequest request
    )
    {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        ValidationError err = new ValidationError(
                Instant.now(), status.value(), "Validation error", request.getRequestURI());

        e.getFieldErrors()
                .forEach(fieldError ->
                {
                    err.addError(fieldError.getField(), fieldError.getDefaultMessage());
                });
        return ResponseEntity.status(status)
                .body(err);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessage> handleAccesDeniedException(
            AccessDeniedException e, HttpServletRequest request
    )
    {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ErrorMessage err = new ErrorMessage(
                Instant.now(), status.value(), "Access denied", request.getRequestURI());
        return ResponseEntity.status(status)
                .body(err);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorMessage> handleBusinessException(
            BusinessException e, HttpServletRequest request
    )
    {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        ErrorMessage err = new ErrorMessage(
                Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status)
                .body(err);
    }
}

