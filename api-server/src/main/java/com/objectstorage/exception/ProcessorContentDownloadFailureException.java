package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when processor content download operation fails.
 */
public class ProcessorContentDownloadFailureException extends IOException {
    public ProcessorContentDownloadFailureException() {
        this("");
    }

    public ProcessorContentDownloadFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage processor content download failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
