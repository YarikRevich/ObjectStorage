package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when ObjectStorage Cluster destruction fails.
 */
public class ClusterDestructionFailureException extends IOException {
    public ClusterDestructionFailureException() {
        this("");
    }

    public ClusterDestructionFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage Cluster destruction failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
