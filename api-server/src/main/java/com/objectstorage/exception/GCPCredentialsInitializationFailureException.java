package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when GCP credentials initialization operation fails.
 */
public class GCPCredentialsInitializationFailureException extends IOException {
    public GCPCredentialsInitializationFailureException() {
        this("");
    }

    public GCPCredentialsInitializationFailureException(Object... message) {
        super(
                new Formatter()
                        .format("GCP credentials initialization failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}