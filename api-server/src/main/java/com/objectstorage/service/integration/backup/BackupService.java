package com.objectstorage.service.integration.backup;

import com.objectstorage.converter.CronExpressionConverter;
import com.objectstorage.converter.RepositoryContentApplicationUnitsToValidationSecretsApplicationConverter;
import com.objectstorage.dto.FolderContentUnitDto;
import com.objectstorage.dto.RepositoryContentApplicationUnitDto;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.model.ContentRetrievalProviderUnit;
import com.objectstorage.model.ValidationSecretsApplication;
import com.objectstorage.repository.facade.RepositoryFacade;
import com.objectstorage.service.state.StateService;
import com.objectstorage.service.telemetry.TelemetryService;
import com.objectstorage.service.vendor.VendorFacade;
import com.objectstorage.service.vendor.common.VendorConfigurationHelper;
import com.objectstorage.service.workspace.facade.WorkspaceFacade;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.objectstorage.service.config.ConfigService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides backup configuration, which will create a local backup of uploaded files.
 */
@Startup(value = 900)
@ApplicationScoped
public class BackupService {
    private static final Logger logger = LogManager.getLogger(BackupService.class);

    @Inject
    PropertiesEntity properties;

    @Inject
    ConfigService configService;

    @Inject
    WorkspaceFacade workspaceFacade;

    @Inject
    VendorFacade vendorFacade;

    @Inject
    RepositoryFacade repositoryFacade;

    @Inject
    TelemetryService telemetryService;

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

                List<RepositoryContentApplicationUnitDto> repositoryContentApplicationUnits;

                try {
                    repositoryContentApplicationUnits = repositoryFacade.retrieveAllContentApplications();
                } catch (ContentApplicationRetrievalFailureException e) {
                    StateService.getBackupProcessorGuard().unlock();

                    return;
                }

                ValidationSecretsApplication validationSecretsApplication =
                        RepositoryContentApplicationUnitsToValidationSecretsApplicationConverter
                                .convert(repositoryContentApplicationUnits);

                String workspaceUnitKey = workspaceFacade.createWorkspaceUnitKey(validationSecretsApplication);

                for (RepositoryContentApplicationUnitDto repositoryContentApplicationUnit :
                        repositoryContentApplicationUnits) {
                    List<ContentRetrievalProviderUnit> contentRetrievalProviderUnits;

                    try {
                        contentRetrievalProviderUnits = vendorFacade.listAllObjectsFromBucket(
                                repositoryContentApplicationUnit.getProvider(),
                                repositoryContentApplicationUnit.getCredentials().getExternal(),
                                VendorConfigurationHelper.createBucketName(
                                        repositoryContentApplicationUnit.getRoot()));
                    } catch (SecretsConversionException | BucketObjectRetrievalFailureException | VendorOperationFailureException e) {
                        StateService.getBackupProcessorGuard().unlock();

                        logger.error(e.getMessage());

                        return;
                    }

                    List<FolderContentUnitDto> folderContentUnits = new ArrayList<>();

                    for (ContentRetrievalProviderUnit contentRetrievalProviderUnit : contentRetrievalProviderUnits) {
                        byte[] content;

                        try {
                            content = vendorFacade.retrieveObjectFromBucket(
                                    repositoryContentApplicationUnit.getProvider(),
                                    repositoryContentApplicationUnit.getCredentials().getExternal(),
                                    VendorConfigurationHelper.createBucketName(
                                            repositoryContentApplicationUnit.getRoot()),
                                    contentRetrievalProviderUnit.getLocation()
                            );
                        } catch (
                                SecretsConversionException |
                                BucketObjectRetrievalFailureException |
                                VendorOperationFailureException e) {
                            StateService.getBackupProcessorGuard().unlock();

                            logger.error(e.getMessage());

                            return;
                        }

                        folderContentUnits.add(FolderContentUnitDto.of(
                                contentRetrievalProviderUnit.getLocation(), content));
                    }

                    if (!folderContentUnits.isEmpty()) {
                        try {
                            workspaceFacade.addBackupFile(
                                    workspaceUnitKey,
                                    workspaceFacade.createBackupFileUnitKey(
                                            repositoryContentApplicationUnit.getProvider().toString(),
                                            properties.getWorkspaceContentBackupUnit()),
                                    folderContentUnits);
                        } catch (FileCreationFailureException e) {
                            StateService.getBackupProcessorGuard().unlock();

                            logger.error(e.getMessage());

                            return;
                        }
                    }

                    telemetryService.increaseCurrentBackupsAmount();
                }

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