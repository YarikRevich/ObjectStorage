package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when ObjectStorage Cluster operation fails.
 */
public class ClusterOperationFailureException extends IOException {
    public ClusterOperationFailureException() {
        this("");
    }

    public ClusterOperationFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage Cluster operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
