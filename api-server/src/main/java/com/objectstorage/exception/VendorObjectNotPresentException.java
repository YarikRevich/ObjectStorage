package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when vendor object is not present.
 */
public class VendorObjectNotPresentException extends IOException {
    public VendorObjectNotPresentException() {
        this("");
    }

    public VendorObjectNotPresentException(Object... message) {
        super(
                new Formatter()
                        .format("Vendor object is not present: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}