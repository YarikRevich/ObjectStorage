package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when key in time limited cache is not found.
 */
public class TimeLimitedCacheKeyNotFoundException extends IOException {
    public TimeLimitedCacheKeyNotFoundException() {
        this("");
    }

    public TimeLimitedCacheKeyNotFoundException(Object... message) {
        super(
                new Formatter()
                        .format("Time limited cache key is not found: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}