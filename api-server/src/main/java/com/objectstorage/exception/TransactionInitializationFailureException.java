package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when database transaction initialization operation fails.
 */
public class TransactionInitializationFailureException extends IOException {
    public TransactionInitializationFailureException() {
        this("");
    }

    public TransactionInitializationFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Transaction initialization failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
