package org.passwddb.servlet.container.model;

public class ServletContainerException extends RuntimeException {
    public ServletContainerException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
