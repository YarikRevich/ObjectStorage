package com.objectstorage.service.processor;

import com.objectstorage.dto.RepositoryContentUnitDto;
import com.objectstorage.dto.TemporateContentUnitDto;
import com.objectstorage.exception.*;
import com.objectstorage.model.*;
import com.objectstorage.repository.executor.RepositoryExecutor;
import com.objectstorage.repository.facade.RepositoryFacade;
import com.objectstorage.service.state.StateService;
import com.objectstorage.service.telemetry.TelemetryService;
import com.objectstorage.service.vendor.VendorFacade;
import com.objectstorage.service.vendor.common.VendorConfigurationHelper;
import com.objectstorage.service.workspace.facade.WorkspaceFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Provides high-level access to ObjectStorage processor operations.
 */
@ApplicationScoped
public class ProcessorService {
    private static final Logger logger = LogManager.getLogger(ProcessorService.class);

    @Inject
    TelemetryService telemetryService;

    @Inject
    RepositoryExecutor repositoryExecutor;

    @Inject
    RepositoryFacade repositoryFacade;

    @Inject
    WorkspaceFacade workspaceFacade;

    @Inject
    VendorFacade vendorFacade;

    /**
     * Retrieves all the content from ObjectStorage Temporate Storage or configured providers.
     *
     * @param validationSecretsApplication given validation secrets application.
     * @return retrieved content.
     * @throws ProcessorContentRetrievalFailureException if content retrieval fails.
     */
    public ContentRetrievalResult retrieveContent(ValidationSecretsApplication validationSecretsApplication)
            throws ProcessorContentRetrievalFailureException {
        List<ContentRetrievalCompound> compounds = new ArrayList<>();

        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            RepositoryContentUnitDto repositoryContentLocationUnitDto;

            try {
                repositoryContentLocationUnitDto = repositoryFacade.retrieveContentApplication(validationSecretsUnit);
            } catch (ContentApplicationRetrievalFailureException e) {
                throw new ProcessorContentRetrievalFailureException(e.getMessage());
            }

            List<ContentRetrievalProviderUnit> pending;

            try {
                pending = repositoryFacade.retrieveFilteredTemporateContent(validationSecretsUnit);
            } catch (TemporateContentRetrievalFailureException e) {
                throw new ProcessorContentRetrievalFailureException(e.getMessage());
            }

            List<ContentRetrievalProviderUnit> uploaded = new ArrayList<>();

            try {
                uploaded = vendorFacade.listAllObjectsFromBucket(
                        validationSecretsUnit.getProvider(),
                        validationSecretsUnit.getCredentials().getExternal(),
                        VendorConfigurationHelper.createBucketName(
                                repositoryContentLocationUnitDto.getRoot()));
            } catch (SecretsConversionException | BucketObjectRetrievalFailureException |
                     VendorOperationFailureException ignored) {
            }

            compounds.add(
                    ContentRetrievalCompound.of(
                            repositoryContentLocationUnitDto.getRoot(),
                            validationSecretsUnit.getProvider().toString(),
                            List.of(ContentRetrievalUnits.of(pending, uploaded))));
        }

        return ContentRetrievalResult.of(compounds);
    }

    /**
     * Applies given content application, creating configured providers buckets, if needed.
     *
     * @param contentApplication given content application.
     * @param validationSecretsApplication given content application.
     * @throws ProcessorContentApplicationFailureException if content application operation fails.
     */
    public void apply(ContentApplication contentApplication, ValidationSecretsApplication validationSecretsApplication)
            throws ProcessorContentApplicationFailureException {
        StateService.getTransactionProcessorGuard().lock();

        try {
            repositoryExecutor.beginTransaction();
        } catch (TransactionInitializationFailureException e) {
            StateService.getTransactionProcessorGuard().unlock();

            throw new ProcessorContentApplicationFailureException(e.getMessage());
        }

        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            try {
                repositoryFacade.apply(contentApplication, validationSecretsUnit);
            } catch (RepositoryContentApplicationFailureException e1) {
                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    throw new ProcessorContentApplicationFailureException(e2.getMessage());
                }

                StateService.getTransactionProcessorGuard().unlock();

                throw new ProcessorContentApplicationFailureException(e1.getMessage());
            }

            try {
                if (!vendorFacade.isBucketPresent(
                        validationSecretsUnit.getProvider(),
                        validationSecretsUnit.getCredentials().getExternal(),
                        VendorConfigurationHelper.createBucketName(
                                contentApplication.getRoot()))) {
                    vendorFacade.createBucket(
                            validationSecretsUnit.getProvider(),
                            validationSecretsUnit.getCredentials().getExternal(),
                            VendorConfigurationHelper.createBucketName(
                                    contentApplication.getRoot()));
                }
            } catch (SecretsConversionException | VendorOperationFailureException e1) {
                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    throw new ProcessorContentApplicationFailureException(e2.getMessage());
                }

                StateService.getTransactionProcessorGuard().unlock();

                throw new ProcessorContentApplicationFailureException(e1.getMessage());
            }
        }

        try {
            repositoryExecutor.commitTransaction();
        } catch (TransactionCommitFailureException e) {
            StateService.getTransactionProcessorGuard().unlock();

            throw new ProcessorContentApplicationFailureException(e.getMessage());
        }

        StateService.getTransactionProcessorGuard().unlock();
    }

    /**
     * Withdraws all the configurations with the given validation secrets application, removing configured providers
     * buckets, if needed.
     *
     * @param validationSecretsApplication given validation secrets application.
     * @throws ProcessorContentWithdrawalFailureException if content withdrawal operation fails.
     */
    public void withdraw(ValidationSecretsApplication validationSecretsApplication)
            throws ProcessorContentWithdrawalFailureException {
        StateService.getTransactionProcessorGuard().lock();

        try {
            repositoryExecutor.beginTransaction();
        } catch (TransactionInitializationFailureException e) {
            StateService.getTransactionProcessorGuard().unlock();

            throw new ProcessorContentWithdrawalFailureException(e.getMessage());
        }

        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            RepositoryContentUnitDto repositoryContentLocationUnitDto;

            try {
                repositoryContentLocationUnitDto = repositoryFacade.retrieveContentApplication(validationSecretsUnit);
            } catch (ContentApplicationRetrievalFailureException e1) {
                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    throw new ProcessorContentWithdrawalFailureException(e2.getMessage());
                }

                StateService.getTransactionProcessorGuard().unlock();

                throw new ProcessorContentWithdrawalFailureException(e1.getMessage());
            }

            try {
                repositoryFacade.withdraw(validationSecretsUnit);
            } catch (RepositoryContentDestructionFailureException e1) {
                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    throw new ProcessorContentWithdrawalFailureException(e2.getMessage());
                }

                StateService.getTransactionProcessorGuard().unlock();

                throw new ProcessorContentWithdrawalFailureException(e1.getMessage());
            }

            try {
                if (vendorFacade.isBucketPresent(
                        validationSecretsUnit.getProvider(),
                        validationSecretsUnit.getCredentials().getExternal(),
                        VendorConfigurationHelper.createBucketName(
                                repositoryContentLocationUnitDto.getRoot()))) {
                    vendorFacade.removeBucket(
                            validationSecretsUnit.getProvider(),
                            validationSecretsUnit.getCredentials().getExternal(),
                            VendorConfigurationHelper.createBucketName(
                                    repositoryContentLocationUnitDto.getRoot()));
                }
            } catch (SecretsConversionException | VendorOperationFailureException e1) {
                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    throw new ProcessorContentWithdrawalFailureException(e2.getMessage());
                }

                StateService.getTransactionProcessorGuard().unlock();

                throw new ProcessorContentWithdrawalFailureException(e1.getMessage());
            }
        }

        try {
            repositoryExecutor.commitTransaction();
        } catch (TransactionCommitFailureException e) {
            StateService.getTransactionProcessorGuard().unlock();

            throw new ProcessorContentWithdrawalFailureException(e.getMessage());
        }

        StateService.getTransactionProcessorGuard().unlock();
    }

    /**
     * Uploads given object content, adding provided input to ObjectStorage Temporate Storage, which will then be
     * processed and added to configured providers.
     *
     * @param location given object file location.
     * @param file given object input file stream.
     * @param validationSecretsApplication given content application.
     * @throws ProcessorContentUploadFailureException if content upload operation fails.
     */
    public void uploadObject(String location, InputStream file, ValidationSecretsApplication validationSecretsApplication)
            throws ProcessorContentUploadFailureException {
        logger.info(String.format("Uploading content at '%s' location", location));

        StateService.getTransactionProcessorGuard().lock();

        try {
            repositoryExecutor.beginTransaction();
        } catch (TransactionInitializationFailureException e) {
            StateService.getTransactionProcessorGuard().unlock();

            throw new ProcessorContentUploadFailureException(e.getMessage());
        }

        String workspaceUnitKey =
                workspaceFacade.createWorkspaceUnitKey(validationSecretsApplication);

        String fileUnitKey = workspaceFacade.createFileUnitKey(location);

        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            try {
                repositoryFacade.upload(location, fileUnitKey, validationSecretsUnit);
            } catch (RepositoryContentApplicationFailureException e1) {
                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    throw new ProcessorContentUploadFailureException(e2.getMessage());
                }

                StateService.getTransactionProcessorGuard().unlock();

                throw new ProcessorContentUploadFailureException(e1.getMessage());
            }
        }

        try {
            workspaceFacade.addObjectFile(workspaceUnitKey, fileUnitKey, file);
        } catch (FileCreationFailureException e1) {
            try {
                repositoryExecutor.rollbackTransaction();
            } catch (TransactionRollbackFailureException e2) {
                StateService.getTransactionProcessorGuard().unlock();

                throw new ProcessorContentUploadFailureException(e2.getMessage());
            }
            StateService.getTransactionProcessorGuard().unlock();

            throw new ProcessorContentUploadFailureException(e1.getMessage());
        }

        try {
            repositoryExecutor.commitTransaction();
        } catch (TransactionCommitFailureException e) {
            StateService.getTransactionProcessorGuard().unlock();

            throw new ProcessorContentUploadFailureException(e.getMessage());
        }

        StateService.getTransactionProcessorGuard().unlock();
    }

    /**
     * Downloads given content object with the help of the given content object download application from
     * ObjectStorage Temporate Storage or configured provider.
     *
     * @param location given content object location.
     * @param validationSecretsUnit given content secrets unit.
     * @param validationSecretsApplication given content secrets application.
     * @return downloaded content object.
     * @throws ProcessorContentDownloadFailureException if content object download operation fails.
     */
    public byte[] downloadObject(
            String location,
            ValidationSecretsUnit validationSecretsUnit,
            ValidationSecretsApplication validationSecretsApplication)
            throws ProcessorContentDownloadFailureException {
        logger.info(String.format("Downloading content object for '%s' location", location));

        String workspaceUnitKey = workspaceFacade.createWorkspaceUnitKey(validationSecretsApplication);

        TemporateContentUnitDto temporateContentUnit;

        try {
            temporateContentUnit =
                    repositoryFacade.retrieveTemporateContentByLocationProviderAndSecret(
                            location, validationSecretsUnit);
        } catch (TemporateContentRemovalFailureException e) {
            throw new ProcessorContentDownloadFailureException(e.getMessage());
        }

        if (Objects.nonNull(temporateContentUnit)) {
            try {
                if (workspaceFacade.isObjectFilePresent(workspaceUnitKey, temporateContentUnit.getHash())) {
                    return workspaceFacade.getObjectFile(workspaceUnitKey, temporateContentUnit.getHash());
                }
            } catch (FileExistenceCheckFailureException | FileUnitRetrievalFailureException e) {
                throw new ProcessorContentDownloadFailureException(e.getMessage());
            }
        }

        RepositoryContentUnitDto repositoryContentLocationUnitDto;

        try {
            repositoryContentLocationUnitDto = repositoryFacade.retrieveContentApplication(validationSecretsUnit);
        } catch (ContentApplicationRetrievalFailureException e) {
            throw new ProcessorContentDownloadFailureException(e.getMessage());
        }

        try {
            if (!vendorFacade.isObjectPresentInBucket(
                    validationSecretsUnit.getProvider(),
                    validationSecretsUnit.getCredentials().getExternal(),
                    VendorConfigurationHelper.createBucketName(
                            repositoryContentLocationUnitDto.getRoot()),
                    location)) {
                throw new ProcessorContentDownloadFailureException(
                        new VendorObjectNotPresentException().getMessage());
            };
        } catch (SecretsConversionException | VendorOperationFailureException e) {
            throw new ProcessorContentDownloadFailureException(e.getMessage());
        }

        try {
            return vendorFacade.retrieveObjectFromBucket(
                    validationSecretsUnit.getProvider(),
                    validationSecretsUnit.getCredentials().getExternal(),
                    VendorConfigurationHelper.createBucketName(
                            repositoryContentLocationUnitDto.getRoot()),
                    location);
        } catch (SecretsConversionException | BucketObjectRetrievalFailureException | VendorOperationFailureException e) {
            throw new ProcessorContentDownloadFailureException(e.getMessage());
        }
    }

    /**
     * Downloads given content backup with the help of the given content backup download application.
     *
     * @param contentBackupDownload given content backup download application.
     * @param validationSecretsApplication given content secrets application.
     * @return downloaded content backup.
     * @throws ProcessorContentDownloadFailureException if content backup download operation fails.
     */
    public byte[] downloadBackup(
            ContentBackupDownload contentBackupDownload,
            ValidationSecretsApplication validationSecretsApplication)
            throws ProcessorContentDownloadFailureException {
        logger.info(String.format("Downloading content backup for '%s' location", contentBackupDownload.getLocation()));

        String workspaceUnitKey = workspaceFacade.createWorkspaceUnitKey(validationSecretsApplication);

        try {
            if (!workspaceFacade.isBackupFilePresent(workspaceUnitKey, contentBackupDownload.getLocation())) {
                throw new ProcessorContentDownloadFailureException(
                        new WorkspaceObjectNotPresentException().getMessage());
            }
        } catch (FileExistenceCheckFailureException e) {
            throw new ProcessorContentDownloadFailureException(e.getMessage());
        }

        try {
            return workspaceFacade.getBackupFile(workspaceUnitKey, contentBackupDownload.getLocation());
        } catch (FileUnitRetrievalFailureException e) {
            throw new ProcessorContentDownloadFailureException(e.getMessage());
        }
    }

    /**
     * Removes content object with the given location from ObjectStorage Temporate Storage or configured provider.
     *
     * @param location given content location.
     * @param validationSecretsApplication given content secrets application.
     * @throws ProcessorContentRemovalFailureException if content removal operation fails.
     */
    public void removeObject(
            String location,
            ValidationSecretsApplication validationSecretsApplication)
            throws ProcessorContentRemovalFailureException {
        logger.info(String.format("Removing content object of '%s' location", location));

        StateService.getTransactionProcessorGuard().lock();

        try {
            repositoryExecutor.beginTransaction();
        } catch (TransactionInitializationFailureException e) {
            StateService.getTransactionProcessorGuard().unlock();

            throw new ProcessorContentRemovalFailureException(e.getMessage());
        }

        String workspaceUnitKey = workspaceFacade.createWorkspaceUnitKey(validationSecretsApplication);

        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            try {
                repositoryFacade.removeTemporateContentByLocationProviderAndSecret(location, validationSecretsUnit);
            } catch (TemporateContentRemovalFailureException e1) {
                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    throw new ProcessorContentRemovalFailureException(e2.getMessage());
                }

                StateService.getTransactionProcessorGuard().unlock();

                throw new ProcessorContentRemovalFailureException(e1.getMessage());
            }

            RepositoryContentUnitDto repositoryContentLocationUnitDto;

            try {
                repositoryContentLocationUnitDto = repositoryFacade.retrieveContentApplication(validationSecretsUnit);
            } catch (ContentApplicationRetrievalFailureException e1) {
                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    throw new ProcessorContentRemovalFailureException(e2.getMessage());
                }

                StateService.getTransactionProcessorGuard().unlock();

                throw new ProcessorContentRemovalFailureException(e1.getMessage());
            }

            try {
                if (!vendorFacade.isObjectPresentInBucket(
                        validationSecretsUnit.getProvider(),
                        validationSecretsUnit.getCredentials().getExternal(),
                        VendorConfigurationHelper.createBucketName(
                                repositoryContentLocationUnitDto.getRoot()),
                        location)) {
                    continue;
                }
            } catch (SecretsConversionException | VendorOperationFailureException e1) {
                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    throw new ProcessorContentRemovalFailureException(e2.getMessage());
                }

                StateService.getTransactionProcessorGuard().unlock();

                throw new ProcessorContentRemovalFailureException(e1.getMessage());
            }

            try {
                vendorFacade.removeObjectFromBucket(
                        validationSecretsUnit.getProvider(),
                        validationSecretsUnit.getCredentials().getExternal(),
                        VendorConfigurationHelper.createBucketName(
                                repositoryContentLocationUnitDto.getRoot()),
                        location);
            } catch (SecretsConversionException | VendorOperationFailureException e1) {
                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    throw new ProcessorContentRemovalFailureException(e2.getMessage());
                }

                StateService.getTransactionProcessorGuard().unlock();

                throw new ProcessorContentRemovalFailureException(e1.getMessage());
            }

            TemporateContentUnitDto temporateContentUnit;

            try {
                temporateContentUnit =
                        repositoryFacade.retrieveTemporateContentByLocationProviderAndSecret(
                                location, validationSecretsUnit);
            } catch (TemporateContentRemovalFailureException e1) {
                try {
                    repositoryExecutor.rollbackTransaction();
                } catch (TransactionRollbackFailureException e2) {
                    StateService.getTransactionProcessorGuard().unlock();

                    throw new ProcessorContentRemovalFailureException(e2.getMessage());
                }

                StateService.getTransactionProcessorGuard().unlock();

                throw new ProcessorContentRemovalFailureException(e1.getMessage());
            }

            if (Objects.nonNull(temporateContentUnit)) {
                try {
                    if (workspaceFacade.isObjectFilePresent(workspaceUnitKey, temporateContentUnit.getHash())) {
                        workspaceFacade.removeObjectFile(workspaceUnitKey, temporateContentUnit.getHash());
                    }
                } catch (FileExistenceCheckFailureException | FileRemovalFailureException e1) {
                    try {
                        repositoryExecutor.rollbackTransaction();
                    } catch (TransactionRollbackFailureException e2) {
                        StateService.getTransactionProcessorGuard().unlock();

                        throw new ProcessorContentRemovalFailureException(e2.getMessage());
                    }

                    StateService.getTransactionProcessorGuard().unlock();

                    throw new ProcessorContentRemovalFailureException(e1.getMessage());
                }
            }
        }

        try {
            repositoryExecutor.commitTransaction();
        } catch (TransactionCommitFailureException e) {
            StateService.getTransactionProcessorGuard().unlock();

            throw new ProcessorContentRemovalFailureException(e.getMessage());
        }

        StateService.getTransactionProcessorGuard().unlock();
    }

    /**
     * Removes all the content from ObjectStorage Temporate Storage or configured provider.
     *
     * @param validationSecretsApplication given content secrets application.
     * @throws ProcessorContentRemovalFailureException if content removal operation fails.
     */
    public void removeAll(ValidationSecretsApplication validationSecretsApplication)
            throws ProcessorContentRemovalFailureException {
        String workspaceUnitKey = workspaceFacade.createWorkspaceUnitKey(validationSecretsApplication);

        try {
            workspaceFacade.removeAll(workspaceUnitKey);
        } catch (FilesRemovalFailureException e) {
            throw new ProcessorContentRemovalFailureException(e.getMessage());
        }

        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            try {
                repositoryFacade.removeTemporateContentByProviderAndSecret(validationSecretsUnit);
            } catch (TemporateContentRemovalFailureException e) {
                throw new ProcessorContentRemovalFailureException(e.getMessage());
            }

            RepositoryContentUnitDto repositoryContentLocationUnitDto;

            try {
                repositoryContentLocationUnitDto = repositoryFacade.retrieveContentApplication(validationSecretsUnit);
            } catch (ContentApplicationRetrievalFailureException e) {
                throw new ProcessorContentRemovalFailureException(e.getMessage());
            }

            try {
                vendorFacade.removeAllObjectsFromBucket(
                        validationSecretsUnit.getProvider(),
                        validationSecretsUnit.getCredentials().getExternal(),
                        VendorConfigurationHelper.createBucketName(
                                repositoryContentLocationUnitDto.getRoot())
                );
            } catch (SecretsConversionException | VendorOperationFailureException e) {
                throw new ProcessorContentRemovalFailureException(e.getMessage());
            }
        }
    }
}