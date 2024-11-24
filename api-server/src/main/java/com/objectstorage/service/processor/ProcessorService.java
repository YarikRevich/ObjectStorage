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
import java.util.Objects;

/**
 * Provides high-level access to ObjectStorage processor operations.
 */
@ApplicationScoped
public class ProcessorService {
    private static final Logger logger = LogManager.getLogger(ProcessorService.class);

    @Inject
    PropertiesEntity properties;

    @Inject
    ConfigService configService;

    @Inject
    ProcessorService processorService;

    @Inject
    TelemetryService telemetryService;

    @Inject
    RepositoryFacade repositoryFacade;

    @Inject
    WorkspaceFacade workspaceFacade;

    @Inject
    VendorFacade vendorFacade;

    /**
     * Retrieves ObjectStorage Cluster content, using given content retrieval application.
     *
     * @param contentRetrievalApplication given content retrieval application.
     * @return retrieved content.
     */
    public ContentRetrievalResult retrieveContent(ValidationSecretsApplication validationSecretsApplication) throws
            ClusterContentRetrievalFailureException {
        StateService.getTopologyStateGuard().lock();

        String workspaceUnitKey =
                workspaceFacade.createUnitKey(
                        contentRetrievalApplication.getProvider(), contentRetrievalApplication.getCredentials());

        List<String> locationUnits;

        try {
            locationUnits = workspaceFacade.getContentUnits(workspaceUnitKey);
        } catch (ContentUnitRetrievalFailureException e) {
            throw new ClusterContentRetrievalFailureException(e.getMessage());
        }

        List<RepositoryContentLocationUnitDto> repositoryContentLocations;

        try {
            repositoryContentLocations =
                    repositoryFacade.retrieveLocations(contentRetrievalApplication);
        } catch (ContentLocationsRetrievalFailureException e) {
            StateService.getTopologyStateGuard().unlock();

            throw new ClusterContentRetrievalFailureException(e.getMessage());
        }

        ContentRetrievalResult result = new ContentRetrievalResult();

        for (String locationUnit : locationUnits) {
            List<String> rawContentUnits;

            try {
                rawContentUnits = workspaceFacade.getRawContentUnits(workspaceUnitKey, locationUnit);
            } catch (RawContentUnitRetrievalFailureException e) {
                StateService.getTopologyStateGuard().unlock();

                throw new ClusterContentRetrievalFailureException(e.getMessage());
            }

            List<String> additionalContentUnits;

            try {
                additionalContentUnits = workspaceFacade.getAdditionalContentUnits(
                        workspaceUnitKey, locationUnit);
            } catch (AdditionalContentUnitRetrievalFailureException e) {
                StateService.getTopologyStateGuard().unlock();

                throw new ClusterContentRetrievalFailureException(e.getMessage());
            }

            Boolean active =
                    repositoryContentLocations
                            .stream()
                            .anyMatch(element -> Objects.equals(element.getLocation(), locationUnit));

            result.addLocationsItem(
                    ContentRetrievalUnit.of(
                            locationUnit,
                            active,
                            ContentRetrievalUnitRaw.of(rawContentUnits),
                            ContentRetrievalUnitAdditional.of(additionalContentUnits)));
        }

        for (RepositoryContentLocationUnitDto repositoryContentLocation : repositoryContentLocations) {
            if (!result
                    .getLocations()
                    .stream()
                    .anyMatch(element ->
                            Objects.equals(
                                    element.getName(), repositoryContentLocation.getLocation()))) {
                result.addLocationsItem(
                        ContentRetrievalUnit.of(
                                repositoryContentLocation.getLocation(),
                                true,
                                ContentRetrievalUnitRaw.of(new ArrayList<>()),
                                ContentRetrievalUnitAdditional.of(new ArrayList<>())));

            }
        }

        StateService.getTopologyStateGuard().unlock();

        return result;
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

//    /**
//     * Removes all the content from the workspace with the help of the given application.
//     *
//     * @param contentCleanupAll given content full cleanup application used for content removal.
//     * @throws ClusterFullCleanupFailureException if ObjectStorage Cluster cleanup failed.
//     */
//    public void removeAll(ContentCleanupAll contentCleanupAll) throws ClusterFullCleanupFailureException {
//        StateService.getTopologyStateGuard().lock();
//
//        String workspaceUnitKey =
//                workspaceFacade.createUnitKey(contentCleanupAll.getProvider(), contentCleanupAll.getCredentials());
//
//        List<ClusterAllocationDto> suspends = new ArrayList<>();
//
//        for (ClusterAllocationDto clusterAllocation : StateService.
//                getClusterAllocationsByWorkspaceUnitKey(workspaceUnitKey)) {
//            logger.info(
//                    String.format(
//                            "Setting ObjectStorage Cluster allocation to suspend state: '%s'",
//                            clusterAllocation.getName()));
//
//            try {
//                clusterCommunicationResource.performSuspend(clusterAllocation.getName());
//
//            } catch (ClusterOperationFailureException e) {
//                logger.fatal(new ClusterFullCleanupFailureException(e.getMessage()).getMessage());
//
//                return;
//            }
//
//            suspends.add(clusterAllocation);
//
//            telemetryService.decreaseServingClustersAmount();
//
//            telemetryService.increaseSuspendedClustersAmount();
//        }
//
//        try {
//            workspaceFacade.removeAll(workspaceUnitKey);
//        } catch (ContentRemovalFailureException e) {
//            StateService.getTopologyStateGuard().unlock();
//
//            throw new ClusterFullCleanupFailureException(e.getMessage());
//        }
//
//        for (ClusterAllocationDto suspended : suspends) {
//            logger.info(
//                    String.format(
//                            "Setting ObjectStorage Cluster suspended allocation to serve state: '%s'",
//                            suspended.getName()));
//
//            try {
//                clusterCommunicationResource.performServe(suspended.getName());
//            } catch (ClusterOperationFailureException e) {
//                logger.fatal(new ClusterFullCleanupFailureException(e.getMessage()).getMessage());
//
//                return;
//            }
//
//            telemetryService.decreaseSuspendedClustersAmount();
//
//            telemetryService.increaseServingClustersAmount();
//        }
//
//        StateService.getTopologyStateGuard().unlock();
//    }
//
}