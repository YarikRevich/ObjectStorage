package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when ObjectStorage Cluster application fails.
 */
public class ClusterApplicationFailureException extends IOException {
    public ClusterApplicationFailureException() {
        this("");
    }

    public ClusterApplicationFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage Cluster application failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
