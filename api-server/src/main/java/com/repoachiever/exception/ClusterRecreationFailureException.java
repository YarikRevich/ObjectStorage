package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when ObjectStorage Cluster recreation operation fails.
 */
public class ClusterRecreationFailureException extends IOException {
    public ClusterRecreationFailureException() {
        this("");
    }

    public ClusterRecreationFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage Cluster recreation operation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
