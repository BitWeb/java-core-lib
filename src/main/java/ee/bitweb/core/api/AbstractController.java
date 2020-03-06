package ee.bitweb.core.api;

import ee.bitweb.core.api.model.error.*;
import ee.bitweb.core.exception.ConflictException;
import ee.bitweb.core.exception.EntityNotFoundException;
import ee.bitweb.core.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractController {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<Object> handleException(HttpMessageNotReadableException e, HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        log.warn(e.getMessage());

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ValidationErrorResponse handleException(MethodArgumentNotValidException e, HttpServletResponse response) {
        return translateBindingResult(e.getBindingResult(), response);
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public ValidationErrorResponse handleException(HttpServletResponse response, BindException e) {
        return translateBindingResult(e.getBindingResult(), response);
    }

    private ValidationErrorResponse translateBindingResult(BindingResult bindingResult, HttpServletResponse response) {
        ValidationErrorResponse validationErrorResponse = new ValidationErrorResponse(
                bindingResult
                        .getFieldErrors()
                        .stream()
                        .map(error -> new ValidationErrorRow(
                                error.getField(),
                                error.getCodes() != null ? error.getCodes()[0].split("\\.")[0] : null,
                                parseMessage(error.getDefaultMessage())
                        ))
                        .collect(Collectors.toSet())
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        return validationErrorResponse;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ValidationErrorResponse handleException(HttpServletResponse response, ConstraintViolationException e) {
        ValidationErrorResponse validationErrorResponse = new ValidationErrorResponse(
                e.getConstraintViolations()
                        .stream()
                        .map(error -> new ValidationErrorRow(
                                getFieldName(error),
                                getValidatorName(error),
                                parseMessage(error.getMessage())
                        ))
                        .collect(Collectors.toSet())
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        return validationErrorResponse;
    }

    private String getFieldName(ConstraintViolation<?> error) {
        String[] parts = error.getPropertyPath().toString().split("\\.");

        return parts[parts.length - 1];
    }

    private String getValidatorName(ConstraintViolation<?> constraintViolation) {
        String validatorName = constraintViolation.getConstraintDescriptor().getAnnotation().annotationType().getName();
        String[] parts = validatorName.split("\\.");

        return parts[parts.length - 1];
    }

    private String parseMessage(String message) {
        if (message != null
                && message.contains("java.util.Date")
                && message.contains("java.lang.IllegalArgumentException")) {
            return message.substring(message.indexOf("java.lang.IllegalArgumentException") + 36);
        }

        return message;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseBody
    public EntityNotFoundErrorResponse handleValidationException(
            HttpServletRequest request, HttpServletResponse response, EntityNotFoundException e
    ) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.NOT_FOUND.value());

        ExceptionInfoLogger.printNotFoundError(request, e);

        return new EntityNotFoundErrorResponse(e);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseBody
    public ConflictErrorResponse handleConflictException(
            HttpServletRequest request,
            HttpServletResponse response,
            ConflictException e
    ) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.CONFLICT.value());

        ExceptionInfoLogger.printConflictError(request, e);

        return new ConflictErrorResponse(e);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public InternalErrorResponse handleGeneralException(
            HttpServletRequest request,
            HttpServletResponse response,
            Throwable e
    ) {
        String errorId = StringUtil.random(20);

        ExceptionInfoLogger.printServerError(request, e, errorId);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        return new InternalErrorResponse(errorId);
    }
}
