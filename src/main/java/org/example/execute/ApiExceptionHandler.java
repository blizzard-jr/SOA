package org.example.execute;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public record ApiError(OffsetDateTime timestamp, int status, String error, String path) {}

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> responseStatus(ResponseStatusException exception, HttpServletRequest request) {
        int status = exception.getStatusCode().value();
        return error(status, request);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class,
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiError> badRequest(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST.value(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> methodNotAllowed(HttpRequestMethodNotSupportedException exception,
                                                       HttpServletRequest request) {
        return error(HttpStatus.METHOD_NOT_ALLOWED.value(), request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> unsupportedMediaType(HttpMediaTypeNotSupportedException exception,
                                                          HttpServletRequest request) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), request);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Void> notAcceptable(HttpMediaTypeNotAcceptableException exception) {
        // A client that rejects JSON cannot receive our JSON ApiError either.
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> routeNotFound(NoResourceFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND.value(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> internalError(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception while processing {}", request.getRequestURI(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR.value(), request);
    }

    private ResponseEntity<ApiError> error(int status, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.valueOf(status);
        ApiError body = new ApiError(OffsetDateTime.now(), status, httpStatus.getReasonPhrase(), request.getRequestURI());
        return ResponseEntity.status(httpStatus).body(body);
    }
}
