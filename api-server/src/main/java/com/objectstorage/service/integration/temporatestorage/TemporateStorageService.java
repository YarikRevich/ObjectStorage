package com.objectstorage.service.integration.temporatestorage;

import com.objectstorage.converter.ContentCompoundUnitsToValidationSecretsApplicationConverter;
import com.objectstorage.converter.CronExpressionConverter;
import com.objectstorage.dto.ContentCompoundUnitDto;
import com.objectstorage.dto.EarliestTemporateContentDto;
import com.objectstorage.exception.*;
import com.objectstorage.model.ValidationSecretsApplication;
import com.objectstorage.repository.executor.RepositoryExecutor;
import com.objectstorage.repository.facade.RepositoryFacade;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.state.StateService;
import com.objectstorage.service.telemetry.TelemetryService;
import com.objectstorage.service.telemetry.binding.TelemetryBinding;
import com.objectstorage.service.vendor.VendorFacade;
import com.objectstorage.service.vendor.common.VendorConfigurationHelper;
import com.objectstorage.service.workspace.facade.WorkspaceFacade;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
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
    RepositoryExecutor repositoryExecutor;

    @Inject
    RepositoryFacade repositoryFacade;

    @Inject
    WorkspaceFacade workspaceFacade;

    @Inject
    VendorFacade vendorFacade;

    @Inject
    TelemetryBinding telemetryBinding;

    @Inject
    TelemetryService telemetryService;

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

        telemetryBinding.getConfiguredTemporateStorageAwaitTime().set(period);

        scheduledOperationExecutorService.scheduleWithFixedDelay(() -> {
            StateService.getTemporateStorageProcessorGuard().lock();

            try {
                if (!repositoryFacade.isTemporateContentPresent()) {
                    StateService.getTemporateStorageProcessorGuard().unlock();

                    return;
                }
            } catch (TemporateContentRetrievalFailureException e1) {
                StateService.getTemporateStorageProcessorGuard().unlock();

                telemetryService.increaseCloudServiceUploadRetries();

                logger.error(e1.getMessage());

                return;
            }

            EarliestTemporateContentDto temporateContentDto;

            try {
                temporateContentDto = repositoryFacade.retrieveEarliestTemporateContent();
            } catch (TemporateContentRetrievalFailureException e) {
                StateService.getTemporateStorageProcessorGuard().unlock();

                telemetryService.increaseCloudServiceUploadRetries();

                logger.error(e.getMessage());

                return;
            }

            StateService.getTransactionProcessorGuard().lock();

            try {
                repositoryExecutor.beginTransaction();
            } catch (TransactionInitializationFailureException e) {
                StateService.getTransactionProcessorGuard().unlock();

                StateService.getTemporateStorageProcessorGuard().unlock();

                telemetryService.increaseCloudServiceUploadRetries();

                logger.error(e.getMessage());

                return;
            }

            try {
                repositoryFacade.removeTemporateContentByHash(temporateContentDto.getHash());
            } catch (TemporateContentRemovalFailureException e1) {
                telemetryService.increaseCloudServiceUploadRetries();

                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    StateService.getTemporateStorageProcessorGuard().unlock();

                    logger.error(e2.getMessage());

                    return;
                }

                StateService.getTransactionProcessorGuard().unlock();

                StateService.getTemporateStorageProcessorGuard().unlock();

                logger.error(e1.getMessage());

                return;
            }

            ValidationSecretsApplication validationSecretsApplication =
                    ContentCompoundUnitsToValidationSecretsApplicationConverter.convert(
                            temporateContentDto.getContentCompoundUnits());

            String workspaceUnitKey = workspaceFacade.createWorkspaceUnitKey(validationSecretsApplication);

            byte[] content;

            try {
                content = workspaceFacade.getObjectFile(workspaceUnitKey, temporateContentDto.getHash());
            } catch (FileUnitRetrievalFailureException e1) {
                telemetryService.increaseCloudServiceUploadRetries();

                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    StateService.getTemporateStorageProcessorGuard().unlock();

                    logger.error(e2.getMessage());

                    return;
                }

                StateService.getTransactionProcessorGuard().unlock();

                StateService.getTemporateStorageProcessorGuard().unlock();

                logger.error(e1.getMessage());

                return;
            }

            for (ContentCompoundUnitDto contentCompoundUnit : temporateContentDto.getContentCompoundUnits()) {
                try {
                    vendorFacade.uploadObjectToBucket(
                            contentCompoundUnit.getProvider(),
                            contentCompoundUnit.getCredentials().getExternal(),
                            VendorConfigurationHelper.createBucketName(
                                    contentCompoundUnit.getRepositoryContentUnitDto().getRoot()),
                            temporateContentDto.getLocation(),
                            new ByteArrayInputStream(content));
                } catch (
                        SecretsConversionException |
                        VendorOperationFailureException |
                        BucketObjectUploadFailureException  e1) {
                    telemetryService.increaseCloudServiceUploadRetries();

                    try {
                        repositoryExecutor.rollbackTransaction();
                    } catch (TransactionRollbackFailureException e2) {
                        StateService.getTransactionProcessorGuard().unlock();

                        StateService.getTemporateStorageProcessorGuard().unlock();

                        logger.error(e2.getMessage());

                        return;
                    }

                    StateService.getTransactionProcessorGuard().unlock();

                    StateService.getTemporateStorageProcessorGuard().unlock();

                    logger.info(e1.getMessage());

                    return;
                }
            }

            try {
                workspaceFacade.removeObjectFile(workspaceUnitKey, temporateContentDto.getHash());
            } catch (FileRemovalFailureException e1) {
                telemetryService.increaseCloudServiceUploadRetries();

                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    StateService.getTemporateStorageProcessorGuard().unlock();

                    logger.error(e2.getMessage());

                    return;
                }

                StateService.getTransactionProcessorGuard().unlock();

                StateService.getTemporateStorageProcessorGuard().unlock();

                logger.error(e1.getMessage());

                return;
            }

            try {
                repositoryExecutor.commitTransaction();
            } catch (TransactionCommitFailureException e) {
                StateService.getTransactionProcessorGuard().unlock();

                StateService.getTemporateStorageProcessorGuard().unlock();

                telemetryService.increaseCloudServiceUploadRetries();

                logger.error(e.getMessage());

                return;
            }

            StateService.getTransactionProcessorGuard().unlock();

            telemetryService.increaseCurrentCloudServiceUploads();

            StateService.getTemporateStorageProcessorGuard().unlock();
        }, 0, period, TimeUnit.MILLISECONDS);
    }

    /**
     * Performs graceful application state cleanup after execution is finished.
     */
    @PreDestroy
    private void close() {
        StateService.getTemporateStorageProcessorGuard().lock();
    }
}

