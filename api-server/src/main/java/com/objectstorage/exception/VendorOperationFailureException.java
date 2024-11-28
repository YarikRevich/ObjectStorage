package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when vendor operation fails.
 */
public class VendorOperationFailureException extends IOException {
    public VendorOperationFailureException() {
        this("");
    }

    public VendorOperationFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Vendor operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}