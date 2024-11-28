package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when processor content removal operation fails.
 */
public class ProcessorContentRemovalFailureException extends IOException {
    public ProcessorContentRemovalFailureException() {
        this("");
    }

    public ProcessorContentRemovalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage processor content removal failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
