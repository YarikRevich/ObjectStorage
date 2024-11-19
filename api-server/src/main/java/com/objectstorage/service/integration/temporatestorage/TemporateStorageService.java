package com.objectstorage.service.integration.temporatestorage;

import com.objectstorage.converter.CronExpressionConverter;
import com.objectstorage.exception.CronExpressionException;
import com.objectstorage.exception.TemporateStoragePeriodRetrievalFailureException;
import com.objectstorage.repository.facade.RepositoryFacade;
import com.objectstorage.service.config.ConfigService;
import io.quarkus.runtime.Startup;
import io.quarkus.vertx.http.HttpServerOptionsCustomizer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

    ScheduledExecutorService operationScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

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

        operationScheduledExecutorService.scheduleWithFixedDelay(() -> {

        }, 0, period, TimeUnit.MILLISECONDS);


        // TODO: run task with some delay
        // TODO: read temporate entities from the db(would be efficient?)
        // TODO: perform cleanup of non existing files.
    }
}

