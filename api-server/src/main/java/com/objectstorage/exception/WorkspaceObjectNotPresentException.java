package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when workspace object is not present.
 */
public class WorkspaceObjectNotPresentException extends IOException {
    public WorkspaceObjectNotPresentException() {
        this("");
    }

    public WorkspaceObjectNotPresentException(Object... message) {
        super(
                new Formatter()
                        .format("Workspace object is not present: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}