package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/** Represents exception, when provided file to be uploaded is invalid. */
public class UploadFileInvalidException extends IOException {
  public UploadFileInvalidException() {
    this("");
  }

  public UploadFileInvalidException(Object... message) {
    super(
        new Formatter()
            .format(
                "Provided file to be uploaded is invalid: %s",
                Arrays.stream(message).toArray())
            .toString());
  }
}
