package com.objectstorage.service.command.internal.health;

import com.objectstorage.entity.ConfigEntity;
import com.objectstorage.exception.*;
import com.objectstorage.model.HealthCheckResult;
import com.objectstorage.model.HealthCheckStatus;
import com.objectstorage.service.client.health.HealthClientService;
import com.objectstorage.service.command.common.ICommand;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.visualization.state.VisualizationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Represents health internal command service.
 */
@Service
public class HealthCheckInternalCommandService implements ICommand<ConfigEntity> {
  @Autowired private VisualizationState visualizationState;

  /**
   * @see ICommand
   */
  @Override
  public void process(ConfigEntity config) throws ApiServerOperationFailureException {
    visualizationState.getLabel().pushNext();

    HealthClientService healthClientService =
            new HealthClientService(config.getApiServer().getHost());

    HealthCheckResult healthCheckResult = healthClientService.process(null);

    if (healthCheckResult.getStatus() == HealthCheckStatus.DOWN) {
      throw new ApiServerOperationFailureException(
          new ApiServerNotAvailableException(healthCheckResult.getChecks().toString())
              .getMessage());
    }

    visualizationState.getLabel().pushNext();
  }
}
