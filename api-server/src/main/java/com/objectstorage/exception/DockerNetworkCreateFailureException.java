package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when Docker network creation fails.
 */
public class DockerNetworkCreateFailureException extends IOException {
    public DockerNetworkCreateFailureException() {
        this("");
    }

    public DockerNetworkCreateFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Docker network creation failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
