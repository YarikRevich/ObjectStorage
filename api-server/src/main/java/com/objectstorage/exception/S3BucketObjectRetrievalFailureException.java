package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when S3 bucket object retrieval operation fails.
 */
public class S3BucketObjectRetrievalFailureException extends IOException {
    public S3BucketObjectRetrievalFailureException() {
        this("");
    }

    public S3BucketObjectRetrievalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("S3 object retrieval operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}