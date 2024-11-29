package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when file units retrieval operation fails.
 */
public class FileUnitsRetrievalFailureException extends IOException {
    public FileUnitsRetrievalFailureException() {
        this("");
    }

    public FileUnitsRetrievalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("File units retrieval operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}