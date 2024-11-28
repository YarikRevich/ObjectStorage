package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when temporate storage period retrieval process fails.
 */
public class TemporateStoragePeriodRetrievalFailureException extends IOException {
    public TemporateStoragePeriodRetrievalFailureException() {
        this("");
    }

    public TemporateStoragePeriodRetrievalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Config file content is not valid: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
