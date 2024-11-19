package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when file units locations retrieval operation fails.
 */
public class FileUnitsLocationsRetrievalFailureException extends IOException {
    public FileUnitsLocationsRetrievalFailureException() {
        this("");
    }

    public FileUnitsLocationsRetrievalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("File units locations retrieval operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}