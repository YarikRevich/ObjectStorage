package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when file unit retrieval operation fails.
 */
public class FileUnitRetrievalFailureException extends IOException {
    public FileUnitRetrievalFailureException() {
        this("");
    }

    public FileUnitRetrievalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("File unit retrieval operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}