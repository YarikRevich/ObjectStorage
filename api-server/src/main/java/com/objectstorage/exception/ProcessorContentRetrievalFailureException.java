package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when processor content retrieval operation fails.
 */
public class ProcessorContentRetrievalFailureException extends IOException {
    public ProcessorContentRetrievalFailureException() {
        this("");
    }

    public ProcessorContentRetrievalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage processor content retrieval failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
