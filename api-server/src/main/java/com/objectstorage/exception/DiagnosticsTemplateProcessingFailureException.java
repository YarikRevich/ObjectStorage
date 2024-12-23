package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when diagnostics template processing fails.
 */
public class DiagnosticsTemplateProcessingFailureException extends IOException {
    public DiagnosticsTemplateProcessingFailureException() {
        this("");
    }

    public DiagnosticsTemplateProcessingFailureException(Object... message) {
        super(
                new Formatter()
                        .format("Diagnostics template processing failed: %s", Arrays.stream(message).toArray())
                        .toString());
    }
}
