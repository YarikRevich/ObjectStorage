package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when bucket object retrieval operation fails.
 */
public class BucketObjectRetrievalFailureException extends IOException {
    public BucketObjectRetrievalFailureException() {
        this("");
    }

    public BucketObjectRetrievalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Object retrieval operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}