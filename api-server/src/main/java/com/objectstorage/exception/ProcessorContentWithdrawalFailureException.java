package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when processor content withdrawal operation fails.
 */
public class ProcessorContentWithdrawalFailureException extends IOException {
    public ProcessorContentWithdrawalFailureException() {
        this("");
    }

    public ProcessorContentWithdrawalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage processor content withdrawal failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
