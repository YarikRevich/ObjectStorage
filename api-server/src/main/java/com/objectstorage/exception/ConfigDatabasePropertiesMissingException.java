package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when configuration file database properties are missing.
 */
public class ConfigDatabasePropertiesMissingException extends IOException {
    public ConfigDatabasePropertiesMissingException() {
        this("");
    }

    public ConfigDatabasePropertiesMissingException(Object... message) {
        super(
                new Formatter()
                        .format("Config file database properties are missing: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
