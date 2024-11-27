package com.objectstorage.service.integration.temporatestorage;

import com.objectstorage.converter.CronExpressionConverter;
import com.objectstorage.dto.TemporateContentDto;
import com.objectstorage.exception.CronExpressionException;
import com.objectstorage.exception.TemporateContentRetrievalFailureException;
import com.objectstorage.exception.TemporateStoragePeriodRetrievalFailureException;
import com.objectstorage.repository.executor.RepositoryExecutor;
import com.objectstorage.repository.facade.RepositoryFacade;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.state.StateService;
import com.objectstorage.service.vendor.VendorFacade;
import com.objectstorage.service.workspace.facade.WorkspaceFacade;
import io.quarkus.runtime.Startup;
import io.quarkus.vertx.http.HttpServerOptionsCustomizer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides temporate storage configuration used for upload processing.
 */
@Startup(value = 800)
@ApplicationScoped
public class TemporateStorageService {
    private static final Logger logger = LogManager.getLogger(TemporateStorageService.class);

    @Inject
    ConfigService configService;

    @Inject
    RepositoryFacade repositoryFacade;

    @Inject
    WorkspaceFacade workspaceFacade;

    @Inject
    VendorFacade vendorFacade;

    private final ScheduledExecutorService scheduledOperationExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    /**
     * Performs temporate storage configuration.
     *
     * @throws TemporateStoragePeriodRetrievalFailureException if temporate storage period retrieval fails.
     */
    @PostConstruct
    public void process() throws TemporateStoragePeriodRetrievalFailureException {
        Long period;

        try {
            period = CronExpressionConverter.convert(
                    configService.getConfig().getTemporateStorage().getFrequency());
        } catch (CronExpressionException e) {
            throw new TemporateStoragePeriodRetrievalFailureException(e.getMessage());
        }

        scheduledOperationExecutorService.scheduleWithFixedDelay(() -> {
            StateService.getTemporateStorageProcessorGuard().lock();

            try {
                if (!repositoryFacade.isTemporateContentPresent()) {
                    StateService.getTemporateStorageProcessorGuard().unlock();

                    return;
                }
            } catch (TemporateContentRetrievalFailureException e) {
                StateService.getTemporateStorageProcessorGuard().unlock();

                logger.fatal(e.getMessage());

                throw new RuntimeException(e);
            }

            TemporateContentDto temporateContentDto;

            try {
                temporateContentDto = repositoryFacade.retrieveEarliestTemporateContent();
            } catch (TemporateContentRetrievalFailureException e) {
                StateService.getTemporateStorageProcessorGuard().unlock();

                logger.fatal(e.getMessage());

                throw new RuntimeException(e);
            }


            System.out.println(temporateContentDto.getHash());

            StateService.getTemporateStorageProcessorGuard().unlock();
        }, 0, period, TimeUnit.MILLISECONDS);

        // TODO: check if bucket exists before upload.
        // TODO: perform cleanup of non existing files.
    }

    /**
     * Performs graceful application state cleanup after execution is finished.
     */
    @PreDestroy
    private void close() {
        StateService.getTemporateStorageProcessorGuard().lock();
    }
}

