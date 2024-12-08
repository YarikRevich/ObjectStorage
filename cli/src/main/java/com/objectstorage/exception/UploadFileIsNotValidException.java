package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/** Represents exception, when provided file to be uploaded is not valid. */
public class UploadFileIsNotValidException extends IOException {
  public UploadFileIsNotValidException() {
    this("");
  }

  public UploadFileIsNotValidException(Object... message) {
    super(
        new Formatter()
            .format(
                "Provided file to be uploaded is not valid: %s",
                Arrays.stream(message).toArray())
            .toString());
  }
}
