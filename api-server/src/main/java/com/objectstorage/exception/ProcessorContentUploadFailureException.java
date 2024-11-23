package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when processor content upload operation fails.
 */
public class ProcessorContentUploadFailureException extends IOException {
    public ProcessorContentUploadFailureException() {
        this("");
    }

    public ProcessorContentUploadFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage processor content upload failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
