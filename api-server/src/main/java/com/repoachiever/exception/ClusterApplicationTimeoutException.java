package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when ObjectStorage Cluster application received timeout.
 */
public class ClusterApplicationTimeoutException extends IOException {
    public ClusterApplicationTimeoutException() {
        this("");
    }

    public ClusterApplicationTimeoutException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage Cluster application received timeout: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
