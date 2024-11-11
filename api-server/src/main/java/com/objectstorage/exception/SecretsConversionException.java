package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when secrets conversion operation fails.
 */
public class SecretsConversionException extends IOException {
  public SecretsConversionException() {
    this("");
  }

  public SecretsConversionException(Object... message) {
    super(
        new Formatter()
            .format("Given secrets are invalid: %s", Arrays.stream(message).toArray())
            .toString());
  }
}
