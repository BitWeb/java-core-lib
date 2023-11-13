package ee.bitweb.core.api;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

import ee.bitweb.core.api.model.exception.FieldErrorResponse;
import ee.bitweb.core.api.model.exception.GenericErrorResponse;
import ee.bitweb.core.api.model.exception.PersistenceErrorResponse;
import ee.bitweb.core.api.model.exception.ValidationErrorResponse;
import ee.bitweb.core.exception.persistence.PersistenceException;
import ee.bitweb.core.exception.validation.InvalidFormatValidationException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import ee.bitweb.core.retrofit.RetrofitException;
import ee.bitweb.core.trace.context.TraceIdContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
@ConditionalOnProperty(value = ControllerAdvisorProperties.PREFIX + ".auto-configuration", havingValue = "true")
public class ControllerAdvisor {

    private static final String DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE;

    private final TraceIdContext traceIdContext;

    private final ControllerAdvisorProperties properties;

    @ResponseBody
    @ExceptionHandler(RetrofitException.class)
    public String handleRetrofitException(
            HttpServletResponse response,
            RetrofitException e
    ) {
        setDefaultHeaders(response, e.getHttpStatus() != null ? e.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR);

        log(properties.getLogging().getRetrofitException(), e.getMessage(), e);

        return e.getErrorBody();
    }

    @ResponseBody
    @ExceptionHandler(PersistenceException.class)
    public PersistenceErrorResponse handleConflictException(
            HttpServletResponse response,
            PersistenceException e
    ) {
        setDefaultHeaders(response, e.getCode());

        log(properties.getLogging().getPersistenceException(), e.getMessage(), e);

        return new PersistenceErrorResponse(getResponseId(), e);
    }

    @ResponseBody
    @ExceptionHandler(MultipartException.class)
    public GenericErrorResponse handleMultipartException(MultipartException e, HttpServletResponse response) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        log(properties.getLogging().getMultipartException(), e.getMessage(), e);

        return new GenericErrorResponse(
                getResponseId(),
                ErrorMessage.CONTENT_TYPE_NOT_VALID.toString()
        );
    }

    @ResponseBody
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public GenericErrorResponse handleException(HttpMediaTypeNotSupportedException e, HttpServletResponse response) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        log(properties.getLogging().getHttpMediaTypeNotSupportedException(), e.getDetailMessageCode(), e);

        return new GenericErrorResponse(
                getResponseId(),
                ErrorMessage.MESSAGE_NOT_READABLE.toString()
        );
    }

    @ResponseBody
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public GenericErrorResponse handleException(HttpMessageNotReadableException e, HttpServletResponse response) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        log(properties.getLogging().getHttpMessageNotReadableException(), e.getMessage(), e);

        InvalidFormatValidationException newException = getFormatValidationException(e);

        if (newException != null && InvalidFormatExceptionConverter.canConvert(newException)) {
            return new ValidationErrorResponse(
                    getResponseId(),
                    InvalidFormatExceptionConverter.convert(newException)
            );
        }

        return new GenericErrorResponse(getResponseId(), ErrorMessage.MESSAGE_NOT_READABLE.toString());
    }

    private InvalidFormatValidationException getFormatValidationException(HttpMessageNotReadableException e) {
        InvalidFormatValidationException newException = null;

        if (e.getCause() instanceof InvalidFormatException invalidFormatException) {
            newException = new InvalidFormatValidationException(
                    invalidFormatException
            );
        } else if (e.getCause() instanceof MismatchedInputException mismatchedInputException) {
            newException = new InvalidFormatValidationException(
                    mismatchedInputException
            );
        }

        return newException;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ValidationErrorResponse handleException(
            HttpServletResponse response,
            ConstraintViolationException e
    ) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        log(properties.getLogging().getConstraintViolationException(), e.getMessage(), e);

        return logAndReturn(
                new ValidationErrorResponse(getResponseId(), ExceptionConverter.convert(e))
        );
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public ValidationErrorResponse handleException(
            BindException e,
            HttpServletResponse response
    ) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        log(properties.getLogging().getBindException(), e.getMessage(), e);

        return logAndReturn(
                new ValidationErrorResponse(
                        getResponseId(),
                        ExceptionConverter.translateBindingResult(e.getBindingResult())
                )
        );
    }

    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ValidationErrorResponse handleException(
            MissingServletRequestParameterException e,
            HttpServletResponse response
    ) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        log(properties.getLogging().getMissingServletRequestParameterException(), e.getMessage(), e);

        return logAndReturn(new ValidationErrorResponse(
                getResponseId(),
                ErrorMessage.INVALID_ARGUMENT.toString(),
                List.of(
                        new FieldErrorResponse(
                                e.getParameterName(),
                                "MissingValue",
                                "Request parameter is required"
                        )
                )
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ValidationErrorResponse handleException(MethodArgumentNotValidException e, HttpServletResponse response) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        log(properties.getLogging().getMethodArgumentNotValidException(), e.getMessage(), e);

        return logAndReturn(new ValidationErrorResponse(
                getResponseId(),
                ExceptionConverter.translateBindingResult(e.getBindingResult())
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ValidationErrorResponse handleException(MethodArgumentTypeMismatchException e, HttpServletResponse response) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        log(properties.getLogging().getMethodArgumentTypeMismatchException(), e.getMessage(), e);

        return logAndReturn(new ValidationErrorResponse(
                getResponseId(),
                ErrorMessage.INVALID_ARGUMENT.toString(),
                Collections.singletonList(
                        new FieldErrorResponse(
                                e.getParameter().getParameterName(),
                                "InvalidType",
                                "Request parameter is invalid"
                        )
                )
        ));
    }

    @ResponseBody
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ValidationErrorResponse handleException(
            MissingServletRequestPartException e,
            HttpServletResponse response
    ) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        log(properties.getLogging().getMissingServletRequestPartException(), e.getRequestPartName(), e);

        return logAndReturn(new ValidationErrorResponse(
                getResponseId(),
                ErrorMessage.INVALID_ARGUMENT.toString(),
                Collections.singletonList(
                        new FieldErrorResponse(
                                e.getRequestPartName(),
                                "RequestPartPresent",
                                String.format("Required request part '%s' is not present", e.getRequestPartName())
                        )
                )
        ));
    }

    @ResponseBody
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public GenericErrorResponse handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException e,
            HttpServletResponse response
    ) {
        setDefaultHeaders(response, HttpStatus.METHOD_NOT_ALLOWED);

        log(properties.getLogging().getHttpRequestMethodNotSupportedException(), e.getDetailMessageCode(), e);

        if (e.getSupportedMethods() != null) {
            response.setHeader(HttpHeaders.ALLOW, String.join(", ", e.getSupportedMethods()));
        }

        return new GenericErrorResponse(
                getResponseId(),
                ErrorMessage.METHOD_NOT_ALLOWED.toString()
        );
    }

    @ExceptionHandler(ClientAbortException.class)
    @ResponseBody
    public GenericErrorResponse clientAbortExceptionHandler(ClientAbortException e, HttpServletResponse response) {
        Throwable cause = NestedExceptionUtils.getRootCause(e);

        if (cause instanceof IOException && cause.getMessage().toLowerCase().contains("broken pipe")) {
            setDefaultHeaders(response, HttpStatus.SERVICE_UNAVAILABLE);

            log(properties.getLogging().getClientAbortException(), e.getMessage(), e);

            return null;
        } else {
            return handleGeneralException(response, e);
        }
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public GenericErrorResponse handleGeneralException(
            HttpServletResponse response,
            Throwable e
    ) {
        setDefaultHeaders(response, HttpStatus.INTERNAL_SERVER_ERROR);

        log.error(e.getMessage(), e);

        return new GenericErrorResponse(
                getResponseId(),
                ErrorMessage.INTERNAL_SERVER_ERROR.toString()
        );
    }

    private String getResponseId() {
        return traceIdContext.get();
    }

    private <T> T logAndReturn(T body) {
        log.debug("{}", body);

        return body;
    }

    private void setDefaultHeaders(HttpServletResponse response, HttpStatus status) {
        setDefaultHeaders(response, status.value());
    }

    private void setDefaultHeaders(HttpServletResponse response, int status) {
        response.setContentType(DEFAULT_CONTENT_TYPE);
        response.setStatus(status);
    }

    private void log(ControllerAdvisorProperties.Level level, String message, Throwable e) {
        switch (level) {
            case ERROR -> log.error(message, e);
            case WARN -> log.warn(message, e);
            case INFO -> log.info(message);
            case DEBUG -> log.debug(message);
            case TRACE -> log.trace(message);
            case OFF -> {
            }
        }
    }
}
