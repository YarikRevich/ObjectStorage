package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when files removal operation fails.
 */
public class FilesRemovalFailureException extends IOException {
    public FilesRemovalFailureException() {
        this("");
    }

    public FilesRemovalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Files removal operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}