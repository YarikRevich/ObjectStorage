package com.objectstorage.service.integration.temporatestorage;

import com.objectstorage.converter.CronExpressionConverter;
import com.objectstorage.exception.CronExpressionException;
import com.objectstorage.exception.TemporateStoragePeriodRetrievalFailureException;
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



            StateService.getTemporateStorageProcessorGuard().unlock();
        }, 0, period, TimeUnit.MILLISECONDS);

        // TODO: make sure bucket is created when application is applied and removed when application is withdrawn.
        // TODO: run task with some delay
        // TODO: read temporate entities from the db(would be efficient?)
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

