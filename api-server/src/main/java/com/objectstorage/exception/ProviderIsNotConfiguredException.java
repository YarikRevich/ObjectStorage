package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when given provider is not configured.
 */
public class ProviderIsNotConfiguredException extends IOException {
    public ProviderIsNotConfiguredException() {
        this("");
    }

    public ProviderIsNotConfiguredException(Object... message) {
        super(
                new Formatter()
                        .format("Provider is not configured: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
