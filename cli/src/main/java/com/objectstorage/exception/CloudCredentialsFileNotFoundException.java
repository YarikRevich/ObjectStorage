package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when given cloud credentials file is not found.
 */
public class CloudCredentialsFileNotFoundException extends IOException {
  public CloudCredentialsFileNotFoundException() {
    this("");
  }

  public CloudCredentialsFileNotFoundException(Object... message) {
    super(
        new Formatter()
            .format(
                "Given cloud credentials file is not found: %s", Arrays.stream(message).toArray())
            .toString());
  }
}
