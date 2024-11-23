package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when backup period retrieval process fails.
 */
public class BackupPeriodRetrievalFailureException extends IOException {
    public BackupPeriodRetrievalFailureException() {
        this("");
    }

    public BackupPeriodRetrievalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Config file content is not valid: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
