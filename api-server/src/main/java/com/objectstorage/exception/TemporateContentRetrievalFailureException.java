package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when temporate content retrieval process fails.
 */
public class TemporateContentRetrievalFailureException extends IOException {
    public TemporateContentRetrievalFailureException() {
        this("");
    }

    public TemporateContentRetrievalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Temporate content retrieval failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
