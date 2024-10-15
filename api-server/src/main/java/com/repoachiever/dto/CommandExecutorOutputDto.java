package com.objectstorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents command executor output.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class CommandExecutorOutputDto {
  private String normalOutput;

  private String errorOutput;
}
