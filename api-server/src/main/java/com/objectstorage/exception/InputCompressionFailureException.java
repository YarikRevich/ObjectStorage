package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when input compression fails.
 */
public class InputCompressionFailureException extends IOException {
    public InputCompressionFailureException() {
        this("");
    }

    public InputCompressionFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Input compression failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
