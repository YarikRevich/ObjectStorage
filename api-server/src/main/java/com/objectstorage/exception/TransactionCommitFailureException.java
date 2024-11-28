package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when database transaction commit operation fails.
 */
public class TransactionCommitFailureException extends IOException {
    public TransactionCommitFailureException() {
        this("");
    }

    public TransactionCommitFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Transaction commit failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
