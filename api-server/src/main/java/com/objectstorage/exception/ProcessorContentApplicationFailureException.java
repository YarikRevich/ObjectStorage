package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when processor content application operation fails.
 */
public class ProcessorContentApplicationFailureException extends IOException {
    public ProcessorContentApplicationFailureException() {
        this("");
    }

    public ProcessorContentApplicationFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage processor content application failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
