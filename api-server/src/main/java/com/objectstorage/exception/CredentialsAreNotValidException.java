package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when given credentials are not valid.
 */
public class CredentialsAreNotValidException extends IOException {
    public CredentialsAreNotValidException() {
        this("");
    }

    public CredentialsAreNotValidException(Object... message) {
        super(
                new Formatter()
                        .format("Credentials are not valid: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
