package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when repository content application does not exist.
 */
public class RepositoryContentApplicationNotExistsException extends IOException {
    public RepositoryContentApplicationNotExistsException() {
        this("");
    }

    public RepositoryContentApplicationNotExistsException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage repository content application does not exist: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
