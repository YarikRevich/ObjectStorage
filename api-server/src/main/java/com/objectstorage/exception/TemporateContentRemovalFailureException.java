package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when temporate content removal process fails.
 */
public class TemporateContentRemovalFailureException extends IOException {
    public TemporateContentRemovalFailureException() {
        this("");
    }

    public TemporateContentRemovalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Temporate content removal failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
