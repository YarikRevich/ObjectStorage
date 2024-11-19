package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when all files amount retrieval fails.
 */
public class AllFilesAmountRetrievalFailureException extends IOException {
    public AllFilesAmountRetrievalFailureException() {
        this("");
    }

    public AllFilesAmountRetrievalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("All files amount retrieval failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}