package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when repository content destruction operation fails.
 */
public class RepositoryContentDestructionFailureException extends IOException {
    public RepositoryContentDestructionFailureException() {
        this("");
    }

    public RepositoryContentDestructionFailureException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage Cluster repository content destruction failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
