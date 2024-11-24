package com.objectstorage.service.processor;

import com.objectstorage.dto.RepositoryContentLocationUnitDto;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.model.*;
import com.objectstorage.repository.facade.RepositoryFacade;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.telemetry.TelemetryService;
import com.objectstorage.service.vendor.VendorFacade;
import com.objectstorage.service.workspace.facade.WorkspaceFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
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
        RepositoryContentLocationUnitDto repositoryContentLocationUnitDto;

        try {
            repositoryContentLocationUnitDto = repositoryFacade.retrieveContent(validationSecretsUnit);
        } catch (ContentLocationsRetrievalFailureException e) {
            throw new ProcessorContentRetrievalFailureException(e.getMessage());
        }

        repositoryFacade.
//
//        String workspaceUnitKey =
//                workspaceFacade.createUnitKey(
//                        contentRetrievalApplication.getProvider(), contentRetrievalApplication.getCredentials());
//
        return ContentRetrievalResult.of(repositoryContentLocationUnitDto.getRoot(), null);
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
        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            try {
                if (!vendorFacade.isBucketPresent(
                        validationSecretsUnit.getProvider(),
                        validationSecretsUnit.getCredentials().getExternal(),
                        contentApplication.getRoot())) {
                    vendorFacade.createBucket(
                            validationSecretsUnit.getProvider(),
                            validationSecretsUnit.getCredentials().getExternal(),
                            contentApplication.getRoot());
                }
            } catch (SecretsConversionException e) {
                throw new ProcessorContentApplicationFailureException(e.getMessage());
            }

            try {
                repositoryFacade.apply(contentApplication, validationSecretsUnit);
            } catch (RepositoryContentApplicationFailureException e) {
                throw new ProcessorContentApplicationFailureException(e.getMessage());
            }
        }
    }

    /**
     * Withdraws all the configurations with the given validation secrets application, removing configured providers
     * buckets, if needed.
     *
     * @param validationSecretsApplication given validation secrets application.
     * @throws ProcessorContentApplicationFailureException if content withdrawal operation fails.
     */
    public void withdraw(ValidationSecretsApplication validationSecretsApplication)
            throws ProcessorContentApplicationFailureException {
        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            RepositoryContentLocationUnitDto repositoryContentLocationUnitDto;

            try {
                repositoryContentLocationUnitDto = repositoryFacade.retrieveContent(validationSecretsUnit);
            } catch (ContentLocationsRetrievalFailureException e) {
                throw new ProcessorContentApplicationFailureException(e.getMessage());
            }

            try {
                if (vendorFacade.isBucketPresent(
                        validationSecretsUnit.getProvider(),
                        validationSecretsUnit.getCredentials().getExternal(),
                        repositoryContentLocationUnitDto.getRoot())) {
                    vendorFacade.removeBucket(
                            validationSecretsUnit.getProvider(),
                            validationSecretsUnit.getCredentials().getExternal(),
                            repositoryContentLocationUnitDto.getRoot());
                }
            } catch (SecretsConversionException e) {
                throw new ProcessorContentApplicationFailureException(e.getMessage());
            }

            try {
                repositoryFacade.withdraw(validationSecretsUnit);
            } catch (RepositoryContentDestructionFailureException e) {
                throw new ProcessorContentApplicationFailureException(e.getMessage());
            }
        }
    }

    /**
     * Uploads given content, adding provided input to ObjectStorage Temporate Storage, which will then be processed and
     * added to configured providers.
     *
     * @param location given file location.
     * @param file given input file stream.
     * @param validationSecretsApplication given content application.
     * @throws ProcessorContentUploadFailureException if content upload operation fails.
     */
    public void upload(String location, InputStream file, ValidationSecretsApplication validationSecretsApplication)
            throws ProcessorContentUploadFailureException {
        logger.info(String.format("Uploading content at '%s' location", location));

        String workspaceUnitKey =
                workspaceFacade.createWorkspaceUnitKey(validationSecretsApplication);

        try {
            workspaceFacade.addFile(workspaceUnitKey, location, file);
        } catch (FileCreationFailureException e) {
            throw new ProcessorContentUploadFailureException(e.getMessage());
        }

        String fileUnitKey = workspaceFacade.createFileUnitKey(location);

        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            try {
                repositoryFacade.upload(location, fileUnitKey, validationSecretsUnit);
            } catch (RepositoryContentApplicationFailureException e) {
                throw new ProcessorContentUploadFailureException(e.getMessage());
            }
        }
    }

    /**
     * Downloads given content with the help of the given content download application from ObjectStorage Temporate
     * Storage or configured provider.
     *
     * @param location given content location.
     * @param validationSecretsUnit given content secrets unit.
     * @param validationSecretsApplication given content secrets application.
     * @return downloaded content.
     * @throws ProcessorContentDownloadFailureException if content download operation fails.
     */
    public byte[] download(
            String location,
            ValidationSecretsUnit validationSecretsUnit,
            ValidationSecretsApplication validationSecretsApplication)
            throws ProcessorContentDownloadFailureException {
        logger.info(String.format("Downloading content for '%s' location", location));

        String workspaceUnitKey = workspaceFacade.createWorkspaceUnitKey(validationSecretsApplication);

        try {
            if (workspaceFacade.isFilePresent(workspaceUnitKey, location)) {
                return workspaceFacade.getFile(workspaceUnitKey, location);
            }
        } catch (FileExistenceCheckFailureException | FileUnitRetrievalFailureException e) {
            throw new ProcessorContentDownloadFailureException(e.getMessage());
        }

        RepositoryContentLocationUnitDto repositoryContentLocationUnitDto;

        try {
            repositoryContentLocationUnitDto = repositoryFacade.retrieveContent(validationSecretsUnit);
        } catch (ContentLocationsRetrievalFailureException e) {
            throw new ProcessorContentDownloadFailureException(e.getMessage());
        }

        try {
            if (!vendorFacade.isObjectPresentInBucket(
                    validationSecretsUnit.getProvider(),
                    validationSecretsUnit.getCredentials().getExternal(),
                    repositoryContentLocationUnitDto.getRoot(),
                    location)) {
                throw new ProcessorContentDownloadFailureException(
                        new VendorObjectNotPresentException().getMessage());
            };
        } catch (SecretsConversionException | BucketObjectRetrievalFailureException e) {
            throw new ProcessorContentDownloadFailureException(e.getMessage());
        }

        try {
            return vendorFacade.retrieveObjectFromBucket(
                    validationSecretsUnit.getProvider(),
                    validationSecretsUnit.getCredentials().getExternal(),
                    repositoryContentLocationUnitDto.getRoot(),
                    location);
        } catch (SecretsConversionException | BucketObjectRetrievalFailureException e) {
            throw new ProcessorContentDownloadFailureException(e.getMessage());
        }
    }

    /**
     * Removes content with the given location from ObjectStorage Temporate Storage or configured provider.
     *
     * @param location given content location.
     * @param validationSecretsApplication given content secrets application.
     * @throws ProcessorContentRemovalFailureException if content removal operation fails.
     */
    public void remove(
            String location,
            ValidationSecretsApplication validationSecretsApplication)
            throws ProcessorContentRemovalFailureException {
        logger.info(String.format("Removing content of '%s' location", location));

        String workspaceUnitKey = workspaceFacade.createWorkspaceUnitKey(validationSecretsApplication);

        try {
            if (workspaceFacade.isFilePresent(workspaceUnitKey, location)) {
                workspaceFacade.removeFile(workspaceUnitKey, location);
            }
        } catch (FileExistenceCheckFailureException | FileRemovalFailureException e) {
            throw new ProcessorContentRemovalFailureException(e.getMessage());
        }

        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            RepositoryContentLocationUnitDto repositoryContentLocationUnitDto;

            try {
                repositoryContentLocationUnitDto = repositoryFacade.retrieveContent(validationSecretsUnit);
            } catch (ContentLocationsRetrievalFailureException e) {
                throw new ProcessorContentRemovalFailureException(e.getMessage());
            }

            try {
                if (!vendorFacade.isObjectPresentInBucket(
                        validationSecretsUnit.getProvider(),
                        validationSecretsUnit.getCredentials().getExternal(),
                        repositoryContentLocationUnitDto.getRoot(),
                        location)) {
                    continue;
                }
            } catch (SecretsConversionException | BucketObjectRetrievalFailureException e) {
                throw new ProcessorContentRemovalFailureException(e.getMessage());
            }

            try {
                vendorFacade.removeObjectFromBucket(
                        validationSecretsUnit.getProvider(),
                        validationSecretsUnit.getCredentials().getExternal(),
                        repositoryContentLocationUnitDto.getRoot(),
                        location);
            } catch (SecretsConversionException e) {
                throw new ProcessorContentRemovalFailureException(e.getMessage());
            }
        }
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
            RepositoryContentLocationUnitDto repositoryContentLocationUnitDto;

            try {
                repositoryContentLocationUnitDto = repositoryFacade.retrieveContent(validationSecretsUnit);
            } catch (ContentLocationsRetrievalFailureException e) {
                throw new ProcessorContentRemovalFailureException(e.getMessage());
            }

            try {
                vendorFacade.removeAllObjectsFromBucket(
                        validationSecretsUnit.getProvider(),
                        validationSecretsUnit.getCredentials().getExternal(),
                        repositoryContentLocationUnitDto.getRoot()
                );
            } catch (SecretsConversionException e) {
                throw new ProcessorContentRemovalFailureException(e.getMessage());
            }
        }
    }
}