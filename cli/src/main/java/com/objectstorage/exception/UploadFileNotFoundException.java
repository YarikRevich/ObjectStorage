package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/** Represents exception, when provided file to be uploaded is not found. */
public class UploadFileNotFoundException extends IOException {
  public UploadFileNotFoundException() {
    this("");
  }

  public UploadFileNotFoundException(Object... message) {
    super(
        new Formatter()
            .format(
                "Provided file to be uploaded is not found: %s",
                Arrays.stream(message).toArray())
            .toString());
  }
}
