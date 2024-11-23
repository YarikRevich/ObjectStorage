package com.objectstorage.service.processor;

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
     * Applies given content, adding provided input to ObjectStorage Temporate Storage, which will then be processed and
     * added to selected provider.
     *
     * @param contentApplication given content application.
     * @param validationSecretsApplication given content application.
     * @throws ProcessorContentApplicationFailureException if content application operation fails.
     */
    public void apply(ContentApplication contentApplication, ValidationSecretsApplication validationSecretsApplication)
            throws ProcessorContentApplicationFailureException {
        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            try {
                if (vendorFacade.isBucketPresent(
                        validationSecretsUnit.getProvider(),
                        validationSecretsUnit.getCredentials().getExternal(),
                        contentApplication.getRoot())) {
                    continue;
                }
            } catch (SecretsConversionException e) {
                throw new ProcessorContentApplicationFailureException(e.getMessage());
            }

            try {
                vendorFacade.createBucket(
                        validationSecretsUnit.getProvider(),
                        validationSecretsUnit.getCredentials().getExternal(),
                        contentApplication.getRoot());
            } catch (SecretsConversionException e) {
                throw new ProcessorContentApplicationFailureException(e.getMessage());
            }
        }

        try {
            repositoryFacade.apply(contentApplication, validationSecretsApplication);
        } catch (RepositoryContentApplicationFailureException e) {
            throw new ProcessorContentApplicationFailureException(e.getMessage());
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

        for (ValidationSecretsUnit validationSecretsUnit : validationSecretsApplication.getSecrets()) {
            try {
                repositoryFacade.upload(location, "hash", validationSecretsUnit);
            } catch (RepositoryContentApplicationFailureException e) {
                throw new ProcessorContentUploadFailureException(e.getMessage());
            }
        }
    }

    /**
     * Downloads given content with the help of the given content download application from Temporate Storage or
     * configured provider.
     *
     * @param contentDownload given content download application.
     * @param validationSecretsApplication given content application.
     * @return downloaded content.
     * @throws ProcessorContentDownloadFailureException if content download operation fails.
     */
    public byte[] download(String location, ValidationSecretsUnit validationSecretsUnit)
            throws ProcessorContentDownloadFailureException {
        logger.info(String.format("Downloading content for '%s' location", location));

        String workspaceUnitKey =
                workspaceFacade.createWorkspaceUnitKey(validationSecretsUnit.getProvider(), validationSecretsUnit.getCredentials());

        Boolean result;

        try {
            result = workspaceFacade.isAnyContentAvailable(workspaceUnitKey, contentDownload.getLocation());
        } catch (ContentAvailabilityRetrievalFailureException e) {
            StateService.getTopologyStateGuard().unlock();

            throw new ClusterContentReferenceRetrievalFailureException(
                    new ClusterContentAvailabilityRetrievalFailureException(e.getMessage()).getMessage());
        }

        if (!result) {
            StateService.getTopologyStateGuard().unlock();

            throw new ClusterContentReferenceRetrievalFailureException(
                    new ClusterContentAvailabilityRetrievalFailureException().getMessage());
        }

        byte[] contentReference;

        try {
            contentReference = workspaceFacade.createContentReference(workspaceUnitKey, contentDownload.getLocation());
        } catch (ContentReferenceCreationFailureException e) {
            StateService.getTopologyStateGuard().unlock();

            throw new ClusterContentReferenceRetrievalFailureException(e.getMessage());
        }

        StateService.getTopologyStateGuard().unlock();

        return contentReference;
    }





//    /**
//     * Applies given content withdrawal, removing existing content configuration with the given properties.
//     *
//     * @param contentWithdrawal given content application used for topology configuration.
//     * @throws ClusterWithdrawalFailureException if ObjectStorage Cluster withdrawal failed.
//     */
//    public void destroy(ContentWithdrawal contentWithdrawal) throws ClusterWithdrawalFailureException {
//        StateService.getTopologyStateGuard().lock();
//
//        String workspaceUnitKey =
//                workspaceFacade.createUnitKey(
//                        contentWithdrawal.getProvider(), contentWithdrawal.getCredentials());
//
//        logger.info(String.format("Destroying ObjectStorage Cluster topology for: '%s'", workspaceUnitKey));
//
//        List<ClusterAllocationDto> clusterAllocations =
//                StateService.getClusterAllocationsByWorkspaceUnitKey(workspaceUnitKey);
//
//        for (ClusterAllocationDto clusterAllocation : clusterAllocations) {
//            try {
//                clusterCommunicationResource.performSuspend(clusterAllocation.getName());
//            } catch (ClusterOperationFailureException e) {
//                StateService.getTopologyStateGuard().unlock();
//
//                throw new ClusterWithdrawalFailureException(e.getMessage());
//            }
//
//            telemetryService.increaseSuspendedClustersAmount();
//
//            telemetryService.decreaseServingClustersAmount();
//
//            logger.info(
//                    String.format("Removing ObjectStorage Cluster allocation: '%s'", clusterAllocation.getName()));
//
//            try {
//                clusterService.destroy(clusterAllocation.getPid());
//            } catch (ClusterDestructionFailureException e) {
//                StateService.getTopologyStateGuard().unlock();
//
//                throw new ClusterWithdrawalFailureException(e.getMessage());
//            }
//
//            telemetryService.decreaseSuspendedClustersAmount();
//        }
//
//        StateService.removeClusterAllocationByNames(
//                clusterAllocations.stream().
//                        map(ClusterAllocationDto::getName).
//                        toList());
//
//        StateService.getTopologyStateGuard().unlock();
//    }
//
//    /**
//     * Destroys all the created ObjectStorage Cluster allocations.
//     *
//     * @throws ClusterFullDestructionFailureException if ObjectStorage Cluster full destruction failed.
//     */
//    public void destroyAll() throws ClusterFullDestructionFailureException {
//        StateService.getTopologyStateGuard().lock();
//
//        logger.info("Destroying all ObjectStorage Cluster topology");
//
//        for (ClusterAllocationDto clusterAllocation : StateService.getClusterAllocations()) {
//            try {
//                clusterCommunicationResource.performSuspend(clusterAllocation.getName());
//            } catch (ClusterOperationFailureException ignored) {
//                logger.info(
//                        String.format("ObjectStorage Cluster allocation is not responding on suspend request: '%s'",
//                                clusterAllocation.getName()));
//            }
//
//            logger.info(
//                    String.format("Removing ObjectStorage Cluster allocation: '%s'", clusterAllocation.getName()));
//
//            try {
//                clusterService.destroy(clusterAllocation.getPid());
//            } catch (ClusterDestructionFailureException e) {
//                throw new ClusterFullDestructionFailureException(e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * Removes content from the workspace with the help of the given application.
//     *
//     * @param contentCleanup given content cleanup application used for content removal.
//     * @throws ClusterCleanupFailureException if ObjectStorage Cluster cleanup failed.
//     */
//    public void removeContent(ContentCleanup contentCleanup) throws ClusterCleanupFailureException {
//        StateService.getTopologyStateGuard().lock();
//
//        logger.info(String.format("Removing content of '%s' location", contentCleanup.getLocation()));
//
//        String workspaceUnitKey =
//                workspaceFacade.createUnitKey(contentCleanup.getProvider(), contentCleanup.getCredentials());
//
//        ClusterAllocationDto clusterAllocation = StateService
//                .getClusterAllocationByWorkspaceUnitKeyAndName(workspaceUnitKey, contentCleanup.getLocation());
//
//        if (Objects.nonNull(clusterAllocation)) {
//            logger.info(
//                    String.format(
//                            "Setting ObjectStorage Cluster allocation to suspend state: '%s'",
//                            clusterAllocation.getName()));
//
//            try {
//                clusterCommunicationResource.performSuspend(clusterAllocation.getName());
//
//            } catch (ClusterOperationFailureException e) {
//                logger.fatal(new ClusterCleanupFailureException(e.getMessage()).getMessage());
//
//                return;
//            }
//
//            telemetryService.decreaseServingClustersAmount();
//
//            telemetryService.increaseSuspendedClustersAmount();
//        }
//
//        try {
//            workspaceFacade.removeContent(workspaceUnitKey, contentCleanup.getLocation());
//        } catch (ContentRemovalFailureException e) {
//            StateService.getTopologyStateGuard().unlock();
//
//            throw new ClusterCleanupFailureException(e.getMessage());
//        }
//
//        if (Objects.nonNull(clusterAllocation)) {
//            logger.info(
//                    String.format(
//                            "Setting ObjectStorage Cluster suspended allocation to serve state: '%s'",
//                            clusterAllocation.getName()));
//
//            try {
//                clusterCommunicationResource.performServe(clusterAllocation.getName());
//            } catch (ClusterOperationFailureException e) {
//                logger.fatal(new ClusterCleanupFailureException(e.getMessage()).getMessage());
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