package com.objectstorage.service.workspace.facade;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.exception.FileNotFoundException;
import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.Provider;
import com.objectstorage.model.ValidationSecretsApplication;
import com.objectstorage.service.workspace.WorkspaceService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides high-level access to workspace operations.
 */
@ApplicationScoped
public class WorkspaceFacade {
    @Inject
    PropertiesEntity properties;

    @Inject
    WorkspaceService workspaceService;

    /**
     * Creates workspace unit key with the help of the given provider and credentials.
     *
     * @param validationSecretsApplication given validation secrets application.
     * @return created workspace unit key.
     */
    public String createWorkspaceUnitKey(ValidationSecretsApplication validationSecretsApplication) {
        return validationSecretsApplication.getSecrets().stream().map(element ->
            switch (element.getProvider()) {
                case S3 -> workspaceService.createUnitKey(
                        element.getProvider().name(),
                        String.valueOf(element.getCredentials().getInternal().getId()),
                        element.getCredentials().getExternal().getFile(),
                        element.getCredentials().getExternal().getRegion());
                case GCS -> workspaceService.createUnitKey(
                        element.getProvider().name(),
                        String.valueOf(element.getCredentials().getInternal().getId()),
                        element.getCredentials().getExternal().getFile());
            })
                .collect(Collectors.joining(""));
    }

    /**
     * Creates file unit key with the help of the given file name and current datetime.
     *
     * @param name given file name.
     * @return created file unit key.
     */
    public String createFileUnitKey(String name) {
        return workspaceService.createUnitKey(name, Instant.now().toString());
    }

    /**
     * Retrieves amount of all the files in the workspace.
     *
     * @return retrieved amount of files in the workspace.
     * @throws AllFilesAmountRetrievalFailureException if all files amount retrieval fails.
     */
    public Integer getAllFilesAmount() throws AllFilesAmountRetrievalFailureException {
        try {
            return workspaceService.getFilesAmount(properties.getWorkspaceDirectory());
        } catch (FilesAmountRetrievalFailureException e) {
            throw new AllFilesAmountRetrievalFailureException(e.getMessage());
        }
    }

    /**
     * Adds new file to the workspace with the given workspace unit key as the compressed input stream.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param name             given content name.
     * @param inputStream          given input.
     * @throws FileCreationFailureException if file creation operation failed.
     */
    public void addFile(String workspaceUnitKey, String name, InputStream inputStream)
            throws FileCreationFailureException {
        if (!workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
            try {
                workspaceService.createUnitDirectory(workspaceUnitKey);
            } catch (WorkspaceUnitDirectoryCreationFailureException e) {
                throw new FileCreationFailureException(e.getMessage());
            }
        }

        String workspaceUnitDirectory;

        try {
            workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
        } catch (WorkspaceUnitDirectoryNotFoundException e) {
            throw new FileCreationFailureException(e.getMessage());
        }

        if (workspaceService.isFilePresent(workspaceUnitDirectory, name)) {
            throw new FileCreationFailureException();
        }

        byte[] content;

        try {
            content = workspaceService.compressFile(inputStream);
        } catch (InputCompressionFailureException e) {
            throw new FileCreationFailureException(e.getMessage());
        }

        try {
            workspaceService.createFile(workspaceUnitDirectory, name, content);
        } catch (FileWriteFailureException e) {
            throw new FileCreationFailureException(e.getMessage());
        }
    }
//
//    /**
//     * Retrieves file units in the workspace with the given workspace unit key.
//     *
//     * @param workspaceUnitKey given user workspace unit key.
//     * @return retrieves file units.
//     * @throws FileUnitsLocationsRetrievalFailureException if file units locations retrieval failed.
//     */
//    public List<String> getFileUnits(String workspaceUnitKey) throws
//            FileUnitsLocationsRetrievalFailureException {
//        List<String> result = new ArrayList<>();
//
//        if (workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
//            String workspaceUnitDirectory;
//
//            try {
//                workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
//            } catch (WorkspaceUnitDirectoryNotFoundException e) {
//                throw new FileUnitsLocationsRetrievalFailureException(e.getMessage());
//            }
//
//            try {
//                result = workspaceService.getFilesLocations(workspaceUnitDirectory);
//            } catch (FilesLocationsRetrievalFailureException e) {
//                throw new FileUnitsLocationsRetrievalFailureException(e.getMessage());
//            }
//        }
//
//        return result;
//    }

    /**
     * Checks if file with the given name exists in the workspace with the given workspace unit key.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param name given file name.
     * @return result of the check.
     * @throws FileExistenceCheckFailureException if file existence check failed.
     */
    public Boolean isFilePresent(String workspaceUnitKey, String name) throws FileExistenceCheckFailureException {
        if (workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
            String workspaceUnitDirectory;

            try {
                workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
            } catch (WorkspaceUnitDirectoryNotFoundException e) {
                throw new FileExistenceCheckFailureException(e.getMessage());
            }

            return workspaceService.isFilePresent(workspaceUnitDirectory, name);
        }

        return false;
    }

    /**
     * Retrieves file from the workspace with the given workspace unit key as compressed byte array.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param name given file name.
     * @return retrieved file as compressed byte array.
     * @throws FileUnitRetrievalFailureException if file unit retrieval fails.
     */
    public byte[] getFile(String workspaceUnitKey, String name) throws FileUnitRetrievalFailureException {
        if (!workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
            throw new FileUnitRetrievalFailureException();
        }

        String workspaceUnitDirectory;

        try {
            workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
        } catch (WorkspaceUnitDirectoryNotFoundException e) {
            throw new FileUnitRetrievalFailureException(e.getMessage());
        }

        try {
            return workspaceService.getFileContent(workspaceUnitDirectory, name);
        } catch (FileNotFoundException e) {
            throw new FileUnitRetrievalFailureException(e.getMessage());
        }
    }

    /**
     * Removes file with the given name from the workspace with the help of the given workspace unit key.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @throws FileRemovalFailureException if file removal operation failed.
     */
    public void removeFile(String workspaceUnitKey, String name) throws FileRemovalFailureException {
        if (workspaceService.isUnitDirectoryExist(workspaceUnitKey)) {
            String workspaceUnitDirectory;

            try {
                workspaceUnitDirectory = workspaceService.getUnitDirectory(workspaceUnitKey);
            } catch (WorkspaceUnitDirectoryNotFoundException e) {
                throw new FileRemovalFailureException(e.getMessage());
            }

            workspaceService.removeFile(workspaceUnitDirectory, name);
        }
    }

    /**
     * Removes all the files from the workspace with the help of the given workspace unit key.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @throws FilesRemovalFailureException if content removal operation failed.
     */
    public void removeAll(String workspaceUnitKey) throws FilesRemovalFailureException {
        try {
            workspaceService.removeUnitDirectory(workspaceUnitKey);
        } catch (WorkspaceUnitDirectoryRemovalFailureException e) {
            throw new FilesRemovalFailureException(e.getMessage());
        }
    }
}
