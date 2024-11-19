package com.objectstorage.exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used to indicate cron expression conversion failure.
 */
public class CronExpressionException extends IOException {
  public CronExpressionException(Object... message) {
    super(
        new Formatter()
            .format("Invalid cron expression: %s", Arrays.stream(message).toArray())
            .toString());
  }
}
