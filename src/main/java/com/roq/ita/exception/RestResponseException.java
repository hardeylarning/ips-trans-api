package com.roq.ita.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class RestResponseException {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorMessage> customException(BadRequestException badRequestException, WebRequest webRequest) {
        ErrorMessage message = new ErrorMessage("4xx", badRequestException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorMessage> notFound(NotFoundException notFoundException, WebRequest webRequest) {
        ErrorMessage message = new ErrorMessage("404", notFoundException.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorMessage> forbidden(ForbiddenException forbiddenException, WebRequest webRequest) {
        ErrorMessage message = new ErrorMessage("4xx", forbiddenException.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
    }

    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<ErrorMessage> badGateway(BadGatewayException badGatewayException, WebRequest webRequest) {
        ErrorMessage message = new ErrorMessage("4xx", badGatewayException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(message);
    }

    @ExceptionHandler(GatewayTimeoutException.class)
    public ResponseEntity<ErrorMessage> badRequest(GatewayTimeoutException gatewayTimeoutException, WebRequest webRequest) {
        ErrorMessage message = new ErrorMessage("5xx", gatewayTimeoutException.getMessage());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(message);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorMessage> internalServer(InternalServerException internalServer, WebRequest webRequest) {
        ErrorMessage message = new ErrorMessage("5xx", internalServer.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorMessage> unauthorizedException(UnauthorizedException unauthorizedException, WebRequest webRequest) {
        ErrorMessage message = new ErrorMessage("401", unauthorizedException.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
    }
}
