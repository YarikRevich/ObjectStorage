package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when jwt token verification operation fails.
 */
public class JwtVerificationFailureException extends IOException {
    public JwtVerificationFailureException() {
        this("");
    }

    public JwtVerificationFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Jwt token verification operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}