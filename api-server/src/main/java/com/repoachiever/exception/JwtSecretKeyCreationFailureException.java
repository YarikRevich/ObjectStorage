package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when jwt secret key creation operation fails.
 */
public class JwtSecretKeyCreationFailureException extends IOException {
    public JwtSecretKeyCreationFailureException() {
        this("");
    }

    public JwtSecretKeyCreationFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Jwt secret key creation operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}