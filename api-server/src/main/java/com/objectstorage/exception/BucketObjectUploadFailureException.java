package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when bucket object upload operation fails.
 */
public class BucketObjectUploadFailureException extends IOException {
    public BucketObjectUploadFailureException() {
        this("");
    }

    public BucketObjectUploadFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Object upload operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}