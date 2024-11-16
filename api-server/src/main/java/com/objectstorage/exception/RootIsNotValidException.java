package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when given root is not valid.
 */
public class RootIsNotValidException extends IOException {
    public RootIsNotValidException() {
        this("");
    }

    public RootIsNotValidException(Object... message) {
        super(
                new Formatter()
                        .format("Root is not valid: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
