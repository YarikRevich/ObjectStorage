package com.objectstorage.service.command.common;

import com.objectstorage.exception.ApiServerOperationFailureException;

/**
 * Represents common command interface.
 *
 * @param <K> type of the command input.
 */
public interface ICommand<K> {
  /**
   * Processes certain request for an external command.
   *
   * @param input input to be given command.
   */
  void process(K input) throws ApiServerOperationFailureException;
}
