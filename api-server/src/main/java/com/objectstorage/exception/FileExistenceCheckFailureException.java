package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when file existence check operation fails.
 */
public class FileExistenceCheckFailureException extends IOException {
    public FileExistenceCheckFailureException() {
        this("");
    }

    public FileExistenceCheckFailureException(Object... message) {
        super(
                new Formatter()
                        .format("File existence check operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}