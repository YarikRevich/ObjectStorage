package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when configuration file was not found.
 */
public class FileNotFoundException extends IOException {
    public FileNotFoundException() {
        this("");
    }

    public FileNotFoundException(Object... message) {
        super(
                new Formatter()
                        .format("File is not found: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
