package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when GCS bucket object upload operation fails.
 */
public class GCSBucketObjectUploadFailureException extends IOException {
    public GCSBucketObjectUploadFailureException() {
        this("");
    }

    public GCSBucketObjectUploadFailureException(Object... message) {
        super(
                new Formatter()
                        .format("GCS object upload operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}