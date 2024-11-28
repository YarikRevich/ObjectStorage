package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when repository content upload operation fails.
 */
public class RepositoryContentUploadFailureException extends IOException {
    public RepositoryContentUploadFailureException() {
        this("");
    }

    public RepositoryContentUploadFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage repository content upload failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
