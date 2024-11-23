package com.objectstorage.service.integration.backup;

import com.objectstorage.converter.CronExpressionConverter;
import com.objectstorage.exception.BackupPeriodRetrievalFailureException;
import com.objectstorage.exception.CronExpressionException;
import com.objectstorage.service.state.StateService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.objectstorage.service.config.ConfigService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides backup configuration, which will create a local backup of uploaded files.
 */
@ApplicationScoped
public class BackupService {
    @Inject
    ConfigService configService;

    private final ScheduledExecutorService scheduledOperationExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    /**
     * Performs backup configuration.
     *
     * @throws BackupPeriodRetrievalFailureException if backup period retrieval fails.
     */
    @PostConstruct
    public void process() throws BackupPeriodRetrievalFailureException {
        if (configService.getConfig().getBackup().getEnabled()) {
            Long period;

            try {
                period = CronExpressionConverter.convert(configService.getConfig().getBackup().getFrequency());
            } catch (CronExpressionException e) {
                throw new BackupPeriodRetrievalFailureException(e.getMessage());
            }

            scheduledOperationExecutorService.scheduleWithFixedDelay(() -> {
                StateService.getBackupProcessorGuard().lock();



                StateService.getBackupProcessorGuard().unlock();
            }, 0, period, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Performs graceful application state cleanup after execution is finished.
     */
    @PreDestroy
    private void close() {
        StateService.getBackupProcessorGuard().lock();
    }
}