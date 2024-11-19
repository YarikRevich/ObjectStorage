package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when file creation operation fails.
 */
public class FileCreationFailureException extends IOException {
    public FileCreationFailureException() {
        this("");
    }

    public FileCreationFailureException(Object... message) {
        super(
                new Formatter()
                        .format("File creation operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}