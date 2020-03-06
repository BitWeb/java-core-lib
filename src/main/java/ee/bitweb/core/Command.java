package ee.bitweb.core;

import ee.bitweb.core.exception.CoreException;

public interface Command<T, R> {
    R execute(T request) throws CoreException;
}
