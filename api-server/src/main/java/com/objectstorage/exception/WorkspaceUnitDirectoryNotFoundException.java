package com.objectstorage.exception;

import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Represents exception used when workspace unit directory was not found.
 */
public class WorkspaceUnitDirectoryNotFoundException extends NoSuchFileException {
  public WorkspaceUnitDirectoryNotFoundException() {
    this("");
  }

  public WorkspaceUnitDirectoryNotFoundException(Object... message) {
    super(
        new Formatter()
            .format("Workspace unit is not found: %s", Arrays.stream(message).toArray())
            .toString());
  }
}
