package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when given secrets are empty.
 */
public class SecretsAreEmptyException extends IOException {
    public SecretsAreEmptyException() {
        this("");
    }

    public SecretsAreEmptyException(Object... message) {
        super(
                new Formatter()
                        .format("Secrets are empty: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
