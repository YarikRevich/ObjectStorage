package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when communication configuration process fails.
 */
public class CommunicationConfigurationFailureException extends IOException {
    public CommunicationConfigurationFailureException() {
        this("");
    }

    public CommunicationConfigurationFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Communication configuration operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}