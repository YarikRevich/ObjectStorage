package com.repoachiever.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when configuration file security properties are missing.
 */
public class ConfigSecurityPropertiesMissingException extends IOException {
    public ConfigSecurityPropertiesMissingException() {
        this("");
    }

    public ConfigSecurityPropertiesMissingException(Object... message) {
        super(
                new Formatter()
                        .format("Config file security properties are missing: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
