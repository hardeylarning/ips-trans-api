package com.roq.ita.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class RestResponseException {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorMessage> customException(CustomException notFoundException, WebRequest webRequest) {
        ErrorMessage message = new ErrorMessage("4xx", notFoundException.getMessage());
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(message);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorMessage> internalServer(InternalServerException internalServer, WebRequest webRequest) {
        ErrorMessage message = new ErrorMessage("5xx", internalServer.getMessage());
        return ResponseEntity.status(500).body(message);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorMessage> unauthorizedException(UnauthorizedException unauthorizedException, WebRequest webRequest) {
        ErrorMessage message = new ErrorMessage("401", unauthorizedException.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
    }
}
