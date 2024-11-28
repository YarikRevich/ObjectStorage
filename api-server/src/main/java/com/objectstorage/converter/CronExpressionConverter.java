package com.objectstorage.converter;

import com.objectstorage.exception.CronExpressionException;
import org.springframework.scheduling.support.CronExpression;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents converter used for cron expression parsing operation.
 */
public class CronExpressionConverter {
  /**
   * Converts frequency from cron expression to milliseconds.
   *
   * @param src cron expression to be converted
   * @return frequency in milliseconds
   */
  public static Long convert(String src) throws CronExpressionException {
    CronExpression cronExpression = CronExpression.parse(src);
    LocalDateTime nextExecutionTime = cronExpression.next(LocalDateTime.now());
    if (Objects.isNull(nextExecutionTime)) {
      throw new CronExpressionException();
    }
    LocalDateTime afterNextExecutionTime = cronExpression.next(nextExecutionTime);
    return Duration.between(nextExecutionTime, afterNextExecutionTime).toMillis();
  }
}
