package com.objectstorage.service.integration.state;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.ApiServerInstanceIsAlreadyRunningException;
import com.objectstorage.exception.RunningStateFileCreationFailureException;
import com.objectstorage.service.integration.common.IntegrationConfigurationHelper;
import com.objectstorage.service.state.StateService;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service used to perform application critical state configuration.
 */
@Startup(value = 100)
@ApplicationScoped
public class StateConfigService {
    private static final Logger logger = LogManager.getLogger(StateConfigService.class);

    @Inject
    PropertiesEntity properties;

    /**
     * Performs application state initialization operations.
     *
     * @throws ApiServerInstanceIsAlreadyRunningException if ObjectStorage API Server has already been initialized.
     * @throws RunningStateFileCreationFailureException   if ObjectStorage API Server running state file creation failed.
     */
    @PostConstruct
    private void process() throws ApiServerInstanceIsAlreadyRunningException, RunningStateFileCreationFailureException {
        Path running = Paths.get(properties.getStateLocation(), properties.getStateRunningName());

        if (Files.exists(running)) {
            throw new ApiServerInstanceIsAlreadyRunningException(
                    IntegrationConfigurationHelper.getRunningStateFileRemovalSuggestionMessage(running));
        }

        try {
            Files.createFile(running);
        } catch (IOException e) {
            throw new RunningStateFileCreationFailureException(e.getMessage());
        }

        StateService.setStarted(true);

        StateService.getStartGuard().countDown();
    }

    /**
     * Performs graceful application state cleanup after execution is finished.
     */
    @PreDestroy
    private void close() {
        if (StateService.getStarted()) {
            try {
                Files.delete(Paths.get(properties.getStateLocation(), properties.getStateRunningName()));
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
