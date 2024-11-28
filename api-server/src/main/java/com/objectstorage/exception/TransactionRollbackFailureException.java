package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when database transaction rollback operation fails.
 */
public class TransactionRollbackFailureException extends IOException {
    public TransactionRollbackFailureException() {
        this("");
    }

    public TransactionRollbackFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Transaction rollback failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
