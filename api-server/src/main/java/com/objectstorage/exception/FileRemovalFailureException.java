package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when file removal operation fails.
 */
public class FileRemovalFailureException extends IOException {
    public FileRemovalFailureException() {
        this("");
    }

    public FileRemovalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("File removal failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}