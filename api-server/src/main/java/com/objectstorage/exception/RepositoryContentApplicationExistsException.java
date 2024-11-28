package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when repository content application already exists.
 */
public class RepositoryContentApplicationExistsException extends IOException {
    public RepositoryContentApplicationExistsException() {
        this("");
    }

    public RepositoryContentApplicationExistsException(Object... message) {
        super(
                new Formatter()
                        .format("ObjectStorage repository content application already exists: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
