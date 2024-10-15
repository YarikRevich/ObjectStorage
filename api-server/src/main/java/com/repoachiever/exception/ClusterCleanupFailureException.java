package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when ObjectStorage Cluster cleanup fails.
 */
public class ClusterCleanupFailureException extends IOException {
    public ClusterCleanupFailureException() {
        this("");
    }

    public ClusterCleanupFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage Cluster cleanup failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
