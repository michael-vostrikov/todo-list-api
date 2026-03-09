package com.todolist.api.exception;

import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Errors are returned as RFC 9457 problem details.
 * Validation errors are put into the "errors" property as field -> message.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERRORS_PROPERTY = "errors";

    private static final String DEFAULT_ERROR_MESSAGE = "Invalid value";

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(EntityValidationException.class)
    public ProblemDetail handleEntityValidation(EntityValidationException e) {
        return validationProblem(HttpStatus.UNPROCESSABLE_ENTITY, e.getErrors());
    }

    @ExceptionHandler(InvalidSortPropertyException.class)
    public ProblemDetail handleInvalidSort(InvalidSortPropertyException e) {
        return validationProblem(HttpStatus.BAD_REQUEST, Map.of("sort", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();

        // a field can have several failed constraints, the first one is enough
        e.getBindingResult().getFieldErrors()
            .forEach(fieldError -> errors.putIfAbsent(fieldError.getField(), messageOf(fieldError)));

        return validationProblem(HttpStatus.BAD_REQUEST, errors);
    }

    // thrown instead of MethodArgumentNotValidException when a handler method has constraints on simple parameters
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleMethodValidation(HandlerMethodValidationException e) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (ParameterValidationResult result : e.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                for (FieldError fieldError : parameterErrors.getFieldErrors()) {
                    errors.putIfAbsent(fieldError.getField(), messageOf(fieldError));
                }
            } else {
                var parameterName = result.getMethodParameter().getParameterName();
                result.getResolvableErrors()
                    .forEach(error -> errors.putIfAbsent(parameterName, messageOf(error)));
            }
        }

        return validationProblem(HttpStatus.BAD_REQUEST, errors);
    }

    private static ProblemDetail validationProblem(HttpStatus status, Map<String, String> errors) {
        var problem = ProblemDetail.forStatusAndDetail(status, "Validation failed");
        problem.setProperty(ERRORS_PROPERTY, errors);
        return problem;
    }

    private static String messageOf(MessageSourceResolvable error) {
        return error.getDefaultMessage() != null ? error.getDefaultMessage() : DEFAULT_ERROR_MESSAGE;
    }
}
