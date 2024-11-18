package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when files amount retrieval fails.
 */
public class FilesAmountRetrievalFailureException extends IOException {
    public FilesAmountRetrievalFailureException() {
        this("");
    }

    public FilesAmountRetrievalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Files amount retrieval failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}