package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when ObjectStorage Cluster deployment fails.
 */
public class ClusterDeploymentFailureException extends IOException {
    public ClusterDeploymentFailureException() {
        this("");
    }

    public ClusterDeploymentFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage Cluster deployment failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
