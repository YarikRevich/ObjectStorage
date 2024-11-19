package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when input decompression fails.
 */
public class InputDecompressionFailureException extends IOException {
    public InputDecompressionFailureException() {
        this("");
    }

    public InputDecompressionFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Input decompression failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
