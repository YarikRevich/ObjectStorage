package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when file write operation fails.
 */
public class FileWriteFailureException extends IOException {
    public FileWriteFailureException() {
        this("");
    }

    public FileWriteFailureException(Object... message) {
        super(
                new Formatter()
                        .format("File write operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
