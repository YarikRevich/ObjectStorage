package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when given credentials are not valid.
 */
public class ProvidersAreNotValidException extends IOException {
    public ProvidersAreNotValidException() {
        this("");
    }

    public ProvidersAreNotValidException(Object... message) {
        super(
                new Formatter()
                        .format("Providers are not valid: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
