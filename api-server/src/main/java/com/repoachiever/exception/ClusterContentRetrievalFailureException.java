package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when ObjectStorage Cluster deployment fails.
 */
public class ClusterContentRetrievalFailureException extends IOException {
    public ClusterContentRetrievalFailureException() {
        this("");
    }

    public ClusterContentRetrievalFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage Cluster content retrieval failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
