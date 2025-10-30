package org.sergej.app.exception;

public class FileLoadException extends RuntimeException {

    public FileLoadException(String message) {
        super(message);
    }

    public FileLoadException(String message, Throwable cause) {
        super(message, cause);
    }

}
