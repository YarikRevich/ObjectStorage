package com.objectstorage.service.workspace.facade;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.Provider;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.workspace.WorkspaceService;
import com.objectstorage.service.workspace.common.WorkspaceConfigurationHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Provides high-level access to workspace operations.
 */
@ApplicationScoped
public class WorkspaceFacade {
    @Inject
    PropertiesEntity properties;

    @Inject
    ConfigService configService;

    @Inject
    WorkspaceService workspaceService;

    /**
     * Creates unit key with the help of the given readiness check application.
     *
     * @param provider          given provider.
     * @param credentialsFields given credentials.
     * @return result of the readiness check for the given configuration.
     */
    public String createUnitKey(Provider provider, CredentialsFieldsFull credentialsFields) {
        return switch (provider) {
            case S3 -> workspaceService.createUnitKey(
                    String.valueOf(credentialsFields.getInternal().getId()),
                    credentialsFields.getExternal().getFile(),
                    credentialsFields.getExternal().getRegion());
            case GCS -> workspaceService.createUnitKey(
                    String.valueOf(credentialsFields.getInternal().getId()),
                    credentialsFields.getExternal().getFile());
        };
    }


    /**
     * Adds new file as the compressed input stream.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param name             given content name.
     * @param inputStream          given input.
     * @throws RawContentCreationFailureException if raw content creation operation failed.
     */
    public void addFile(String workspaceUnitKey, String name, InputStream inputStream)
            throws RawContentCreationFailureException {
        if (!workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
            try {
                workspaceService.createUnitDirectory(workspaceUnitKey);
            } catch (WorkspaceUnitDirectoryCreationFailureException e) {
                throw new RawContentCreationFailureException(e.getMessage());
            }
        }

        String workspaceUnitDirectory;

        try {
            workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
        } catch (WorkspaceUnitDirectoryNotFoundException e) {
            throw new RawContentCreationFailureException(e.getMessage());
        }

        if (workspaceService.isFilePresent(workspaceUnitDirectory, name)) {
            throw new RawContentCreationFailureException();
        }

        try {
            workspaceService.createFile(workspaceUnitDirectory, name, inputStream);
        } catch (RawContentFileWriteFailureException e) {
            throw new RawContentCreationFailureException(e.getMessage());
        }
    }
//
//    /**
//     * Checks if raw content file exists with the given location and the given name.
//     *
//     * @param workspaceUnitKey given user workspace unit key.
//     * @param location         given content location.
//     * @param name             given content name.
//     * @return result of the check.
//     * @throws RawContentRetrievalFailureException if raw content retrieval operation failed.
//     */
//    public Boolean isRawContentPresent(String workspaceUnitKey, String location, String name) throws
//            RawContentRetrievalFailureException {
//        if (!workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
//            return false;
//        }
//
//        String workspaceUnitDirectory;
//
//        try {
//            workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
//        } catch (WorkspaceUnitDirectoryNotFoundException e) {
//            throw new RawContentRetrievalFailureException(e.getMessage());
//        }
//
//        return workspaceService.isRawContentFileExist(workspaceUnitDirectory, location, name);
//    }
//
//    /**
//     * Adds new version of raw content file as the raw input stream.
//     *
//     * @param workspaceUnitKey given user workspace unit key.
//     * @param location         given content location.
//     * @param name             given content name.
//     * @param content          given content input.
//     * @throws AdditionalContentCreationFailureException if additional content creation operation failed.
//     */
//    public void addAdditionalContent(
//            String workspaceUnitKey, String location, String name, AdditionalContentFileEntity content) throws
//            AdditionalContentCreationFailureException {
//        if (!workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
//            try {
//                workspaceService.createUnitDirectory(workspaceUnitKey);
//            } catch (WorkspaceUnitDirectoryCreationFailureException e) {
//                throw new AdditionalContentCreationFailureException(e.getMessage());
//            }
//        }
//
//        String workspaceUnitDirectory;
//
//        try {
//            workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
//        } catch (WorkspaceUnitDirectoryNotFoundException e) {
//            throw new AdditionalContentCreationFailureException(e.getMessage());
//        }
//
//        if (!workspaceService.isContentDirectoryExist(workspaceUnitDirectory, location)) {
//            try {
//                workspaceService.createContentDirectory(workspaceUnitDirectory, location);
//            } catch (WorkspaceContentDirectoryCreationFailureException e) {
//                throw new AdditionalContentCreationFailureException(e.getMessage());
//            }
//        }
//
//        if (!workspaceService.isAdditionalContentDirectoryExist(workspaceUnitDirectory, location)) {
//            try {
//                workspaceService.createAdditionalContentDirectory(workspaceUnitDirectory, location);
//            } catch (WorkspaceContentDirectoryCreationFailureException e) {
//                throw new AdditionalContentCreationFailureException(e.getMessage());
//            }
//        }
//
//        Integer amount;
//
//        try {
//            amount = workspaceService.getAdditionalContentFilesAmount(workspaceUnitDirectory, location);
//        } catch (AdditionalContentFilesAmountRetrievalFailureException e) {
//            throw new AdditionalContentCreationFailureException(e.getMessage());
//        }
//
//        try {
//            workspaceService.createAdditionalContentFile(workspaceUnitDirectory, location, name, content);
//        } catch (AdditionalContentFileWriteFailureException e) {
//            throw new AdditionalContentCreationFailureException(e.getMessage());
//        }
//
//        while (amount > configService.getConfig().getResource().getCluster().getMaxVersions() - 1) {
//            try {
//                workspaceService.removeEarliestRawContentFile(workspaceUnitDirectory, location);
//            } catch (RawContentFileRemovalFailureException e) {
//                throw new AdditionalContentCreationFailureException(e.getMessage());
//            }
//
//            amount--;
//        }
//    }
//
//    /**
//     * Checks if additional content file exists with the given location and the given name.
//     *
//     * @param workspaceUnitKey given user workspace unit key.
//     * @param location         given content location.
//     * @param name             given content name.
//     * @return result of the check.
//     * @throws AdditionalContentRetrievalFailureException if additional content retrieval operation failed.
//     */
//    public Boolean isAdditionalContentPresent(String workspaceUnitKey, String location, String name) throws
//            AdditionalContentRetrievalFailureException {
//        if (!workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
//            return false;
//        }
//
//        String workspaceUnitDirectory;
//
//        try {
//            workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
//        } catch (WorkspaceUnitDirectoryNotFoundException e) {
//            throw new AdditionalContentRetrievalFailureException(e.getMessage());
//        }
//
//        return workspaceService.isAdditionalContentFileExist(workspaceUnitDirectory, location, name);
//    }
//
//    /**
//     * Removes all the content from the workspace with the help of the given workspace unit key.
//     *
//     * @param workspaceUnitKey given user workspace unit key.
//     * @throws ContentRemovalFailureException if content removal operation failed.
//     */
//    public void removeContent(String workspaceUnitKey, String location) throws ContentRemovalFailureException {
//        if (workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
//            String workspaceUnitDirectory;
//
//            try {
//                workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
//            } catch (WorkspaceUnitDirectoryNotFoundException e) {
//                throw new ContentRemovalFailureException(e.getMessage());
//            }
//
//            try {
//                workspaceService.removeContentDirectory(workspaceUnitDirectory, location);
//            } catch (ContentDirectoryRemovalFailureException e) {
//                throw new ContentRemovalFailureException(e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * Removes all the content from the workspace with the help of the given workspace unit key.
//     *
//     * @param workspaceUnitKey given user workspace unit key.
//     * @throws ContentRemovalFailureException if content removal operation failed.
//     */
//    public void removeAll(String workspaceUnitKey) throws ContentRemovalFailureException {
//        try {
//            workspaceService.removeUnitDirectory(workspaceUnitKey);
//        } catch (WorkspaceUnitDirectoryRemovalFailureException e) {
//            throw new ContentRemovalFailureException(e.getMessage());
//        }
//    }
//
//    /**
//     * Checks if raw content exists in the workspace with the given workspace unit directory and location.
//     *
//     * @param workspaceUnitDirectory given workspace unit directory.
//     * @param location               given content location.
//     * @return result of the check.
//     * @throws ContentAvailabilityRetrievalFailureException if raw content availability retrieval fails.
//     */
//    private Boolean isRawContentAvailable(String workspaceUnitDirectory, String location) throws
//            ContentAvailabilityRetrievalFailureException {
//        Boolean rawResult = false;
//
//        if (workspaceService.isContentDirectoryExist(workspaceUnitDirectory, location)) {
//            if (workspaceService.isRawContentDirectoryExist(workspaceUnitDirectory, location)) {
//                Integer rawAmount;
//
//                try {
//                    rawAmount = workspaceService.getRawContentFilesAmount(workspaceUnitDirectory, location);
//                } catch (RawContentFilesAmountRetrievalFailureException e) {
//                    throw new ContentAvailabilityRetrievalFailureException(e.getMessage());
//                }
//
//                rawResult = rawAmount != 0;
//            }
//        }
//
//        return rawResult;
//    }
//
//    /**
//     * Checks if additional content exists in the workspace with the given workspace unit directory and location.
//     *
//     * @param workspaceUnitDirectory given workspace unit directory.
//     * @param location               given content location.
//     * @return result of the check.
//     * @throws ContentAvailabilityRetrievalFailureException if additional content availability retrieval fails.
//     */
//    private Boolean isAdditionalContentAvailable(String workspaceUnitDirectory, String location) throws
//            ContentAvailabilityRetrievalFailureException {
//        Boolean additionalResult = false;
//
//        if (workspaceService.isContentDirectoryExist(workspaceUnitDirectory, location)) {
//            if (workspaceService.isAdditionalContentDirectoryExist(workspaceUnitDirectory, location)) {
//                Integer additionalAmount;
//
//                try {
//                    additionalAmount = workspaceService.getAdditionalContentFilesAmount(workspaceUnitDirectory, location);
//                } catch (AdditionalContentFilesAmountRetrievalFailureException e) {
//                    throw new ContentAvailabilityRetrievalFailureException(e.getMessage());
//                }
//
//                additionalResult = additionalAmount != 0;
//            }
//        }
//
//        return additionalResult;
//    }
//
//    /**
//     * Checks if given content exists in the workspace with the given workspace unit key and location.
//     *
//     * @param workspaceUnitKey given user workspace unit key.
//     * @param location         given content location.
//     * @return result of the check.
//     * @throws ContentAvailabilityRetrievalFailureException if content availability retrieval fails.
//     */
//    public Boolean isAnyContentAvailable(String workspaceUnitKey, String location) throws
//            ContentAvailabilityRetrievalFailureException {
//        if (!workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
//            return false;
//        }
//
//        String workspaceUnitDirectory;
//
//        try {
//            workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
//        } catch (WorkspaceUnitDirectoryNotFoundException e) {
//            throw new ContentAvailabilityRetrievalFailureException(e.getMessage());
//        }
//
//        return isRawContentAvailable(workspaceUnitDirectory, location) ||
//                isAdditionalContentAvailable(workspaceUnitDirectory, location);
//    }
//
//    /**
//     * Retrieves content units in the workspace with the given workspace unit key and location.
//     *
//     * @param workspaceUnitKey given user workspace unit key.
//     * @return retrieves content units.
//     * @throws ContentUnitRetrievalFailureException if content unit retrieval failed.
//     */
//    public List<String> getContentUnits(String workspaceUnitKey) throws
//            ContentUnitRetrievalFailureException {
//        List<String> result = new ArrayList<>();
//
//        if (workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
//            String workspaceUnitDirectory;
//
//            try {
//                workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
//            } catch (WorkspaceUnitDirectoryNotFoundException e) {
//                throw new ContentUnitRetrievalFailureException(e.getMessage());
//            }
//
//            try {
//                result = workspaceService.getContentUnitsLocations(workspaceUnitDirectory);
//            } catch (ContentUnitsLocationsRetrievalFailureException e) {
//                throw new ContentUnitRetrievalFailureException(e.getMessage());
//            }
//        }
//
//        return result;
//    }
//
//    /**
//     * Retrieves raw content units in the workspace with the given workspace unit key and location.
//     *
//     * @param workspaceUnitKey given user workspace unit key.
//     * @param location         given content location.
//     * @return retrieves raw content units.
//     * @throws RawContentUnitRetrievalFailureException if raw content unit retrieval failed.
//     */
//    public List<String> getRawContentUnits(String workspaceUnitKey, String location) throws
//            RawContentUnitRetrievalFailureException {
//        List<String> result = new ArrayList<>();
//
//        if (workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
//            String workspaceUnitDirectory;
//
//            try {
//                workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
//            } catch (WorkspaceUnitDirectoryNotFoundException e) {
//                throw new RawContentUnitRetrievalFailureException(e.getMessage());
//            }
//
//            Boolean available;
//
//            try {
//                available = isRawContentAvailable(workspaceUnitDirectory, location);
//            } catch (ContentAvailabilityRetrievalFailureException e) {
//                throw new RawContentUnitRetrievalFailureException(e.getMessage());
//            }
//
//            if (available) {
//                try {
//                    result = workspaceService.getRawContentFilesLocations(workspaceUnitDirectory, location);
//                } catch (ContentFilesLocationsRetrievalFailureException e) {
//                    throw new RawContentUnitRetrievalFailureException(e.getMessage());
//                }
//            }
//        }
//
//        return result;
//    }
//
//    /**
//     * Retrieves additional content units in the workspace with the given workspace unit key and location.
//     *
//     * @param workspaceUnitKey given user workspace unit key.
//     * @param location         given content location.
//     * @return retrieves additional content units.
//     * @throws AdditionalContentUnitRetrievalFailureException if additional content unit retrieval failed.
//     */
//    public List<String> getAdditionalContentUnits(String workspaceUnitKey, String location) throws
//            AdditionalContentUnitRetrievalFailureException {
//        List<String> result = new ArrayList<>();
//
//        if (workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
//            String workspaceUnitDirectory;
//
//            try {
//                workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
//            } catch (WorkspaceUnitDirectoryNotFoundException e) {
//                throw new AdditionalContentUnitRetrievalFailureException(e.getMessage());
//            }
//
//            Boolean available;
//
//            try {
//                available = isAdditionalContentAvailable(workspaceUnitDirectory, location);
//            } catch (ContentAvailabilityRetrievalFailureException e) {
//                throw new AdditionalContentUnitRetrievalFailureException(e.getMessage());
//            }
//
//            if (available) {
//                try {
//                    result = workspaceService.getAdditionalContentFilesLocations(workspaceUnitDirectory, location);
//                } catch (ContentFilesLocationsRetrievalFailureException e) {
//                    throw new AdditionalContentUnitRetrievalFailureException(e.getMessage());
//                }
//            }
//        }
//
//        return result;
//    }
//
}
