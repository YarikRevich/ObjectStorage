package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when given cloud credentials are not valid.
 */
public class CloudCredentialsValidationException extends IOException {
  public CloudCredentialsValidationException() {
    this("");
  }

  public CloudCredentialsValidationException(Object... message) {
    super(
        new Formatter()
            .format("Given cloud credentials are not valid: %s", Arrays.stream(message).toArray())
            .toString());
  }
}
