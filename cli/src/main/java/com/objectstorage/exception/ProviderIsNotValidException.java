package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/** Represents exception, when given provider is not valid. */
public class ProviderIsNotValidException extends IOException {
    public ProviderIsNotValidException() {
        this("");
    }

    public ProviderIsNotValidException(Object... message) {
        super(
                new Formatter()
                        .format(
                                "Given provider is not valid: %s",
                                Arrays.stream(message).toArray())
                        .toString());
    }
}
