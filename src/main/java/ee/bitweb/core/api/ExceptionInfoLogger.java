package ee.bitweb.core.api;

import ee.bitweb.core.exception.CoreException;
import ee.bitweb.core.exception.ConflictException;
import ee.bitweb.core.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public final class ExceptionInfoLogger {

    // Helper class, no need to create instance
    private ExceptionInfoLogger() {
    }

    public static void printNotFoundError(HttpServletRequest request, EntityNotFoundException e) {
        print(request, HttpStatus.NOT_FOUND.value(), e);
    }

    public static void printConflictError(HttpServletRequest request, ConflictException e) {
        print(request, HttpStatus.CONFLICT.value(), e);
    }

    private static void print(HttpServletRequest request, int status, CoreException e) {
        log.warn("{} {}: {}", getPath(request), status, e.getMessage());
    }

    public static void printServerError(HttpServletRequest request, Throwable e, String errorId) {
        log.error(String.format(
                "ID: '%s', request: '%s', message: '%s'",
                errorId,
                getPath(request),
                e.getMessage()
        ), e);
    }

    private static String getPath(HttpServletRequest request) {
        String path = request.getMethod() + " " + request.getRequestURI();

        if (request.getQueryString() != null) path += "?" + request.getQueryString();

        return path;
    }
}
