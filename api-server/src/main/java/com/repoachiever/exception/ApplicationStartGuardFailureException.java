package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when ObjectStorage API Server application start guard fails.
 */
public class ApplicationStartGuardFailureException extends IOException {
    public ApplicationStartGuardFailureException() {
        this("");
    }

    public ApplicationStartGuardFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage API Server application start guard operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
