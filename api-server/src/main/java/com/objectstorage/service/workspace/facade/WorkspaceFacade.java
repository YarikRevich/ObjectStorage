package com.objectstorage.service.workspace.facade;

import com.objectstorage.dto.FolderContentUnitDto;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.model.ContentRetrievalBackupUnit;
import com.objectstorage.model.ValidationSecretsApplication;
import com.objectstorage.service.config.ConfigService;
import com.objectstorage.service.workspace.WorkspaceService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.*;
import java.time.Instant;
import java.util.Comparator;
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
    ConfigService configService;

    @Inject
    WorkspaceService workspaceService;

    /**
     * Creates workspace unit key with the help of the given provider and credentials.
     *
     * @param validationSecretsApplication given validation secrets application.
     * @return created workspace unit key.
     */
    public String createWorkspaceUnitKey(ValidationSecretsApplication validationSecretsApplication) {
        return validationSecretsApplication.getSecrets().stream()
                .sorted(Comparator.comparing(element -> element.getProvider().toString()))
                .map(element ->
            switch (element.getProvider()) {
                case S3 -> workspaceService.createUnitKey(
                        element.getProvider().toString(),
                        String.valueOf(element.getCredentials().getInternal().getId()),
                        element.getCredentials().getExternal().getFile(),
                        element.getCredentials().getExternal().getRegion());
                case GCS -> workspaceService.createUnitKey(
                        element.getProvider().toString(),
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
        Instant timestamp = Instant.now();

        String fileUnit =
                workspaceService.createUnitKey(name, Instant.now().toString());

        return String.format("%s-%d", fileUnit, timestamp.toEpochMilli());
    }

    /**
     * Adds new object file to the workspace with the given workspace unit key as the compressed input stream.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param name             given content name.
     * @param inputStream          given input.
     * @throws FileCreationFailureException if file creation operation failed.
     */
    public void addObjectFile(String workspaceUnitKey, String name, InputStream inputStream)
            throws FileCreationFailureException {
        byte[] content;

        try {
            content = workspaceService.compressFile(inputStream);
        } catch (InputCompressionFailureException e) {
            throw new FileCreationFailureException(e.getMessage());
        }

        workspaceService.addContentFile(
                workspaceUnitKey,
                properties.getWorkspaceContentObjectDirectory(),
                name,
                content);
    }

    /**
     * Adds new backup file to the workspace with the given workspace unit key as the compressed input stream.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param name given file name.
     * @param folderContentUnits          given folder content units.
     * @throws FileCreationFailureException if file creation operation failed.
     */
    public void addBackupFile(String workspaceUnitKey, String name, List<FolderContentUnitDto> folderContentUnits)
            throws FileCreationFailureException {
        byte[] content;

        try {
            content =
                    workspaceService.compressFolder(folderContentUnits, properties.getWorkspaceContentBackupDirectory());
        } catch (InputCompressionFailureException e) {
            throw new FileCreationFailureException(e.getMessage());
        }

        workspaceService.addContentFile(
                workspaceUnitKey,
                properties.getWorkspaceContentBackupDirectory(),
                name,
                content);

        Integer amount;

        try {
            amount = workspaceService.getContentFilesAmount(
                    workspaceUnitKey, properties.getWorkspaceContentBackupDirectory());
        } catch (FilesAmountRetrievalFailureException e) {
            throw new FileCreationFailureException(e.getMessage());
        }

        while (amount > configService.getConfig().getBackup().getMaxVersions() - 1) {
            try {
                workspaceService.removeEarliestContentFile(
                        workspaceUnitKey, properties.getWorkspaceContentBackupDirectory());
            } catch (FileRemovalFailureException e) {
                throw new FileCreationFailureException(e.getMessage());
            }

            amount--;
        }
    }

    /**
     * Checks if object file with the given name exists in the workspace with the given workspace unit key.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param name given file name.
     * @return result of the check.
     * @throws FileExistenceCheckFailureException if file existence check failed.
     */
    public Boolean isObjectFilePresent(String workspaceUnitKey, String name) throws FileExistenceCheckFailureException {
        return workspaceService.isContentFilePresent(
                workspaceUnitKey, properties.getWorkspaceContentObjectDirectory(), name);
    }

    /**
     * Checks if backup file with the given name exists in the workspace with the given workspace unit key.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param name given file name.
     * @return result of the check.
     * @throws FileExistenceCheckFailureException if file existence check failed.
     */
    public Boolean isBackupFilePresent(String workspaceUnitKey, String name) throws FileExistenceCheckFailureException {
        return workspaceService.isContentFilePresent(
                workspaceUnitKey, properties.getWorkspaceContentBackupDirectory(), name);
    }

    /**
     * Retrieves backup units from the workspace with the given workspace unit key.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @return retrieved backup units.
     * @throws FileUnitsRetrievalFailureException if file units retrieval fails.
     */
    public List<ContentRetrievalBackupUnit> getBackupUnits(String workspaceUnitKey) throws FileUnitsRetrievalFailureException {
        return workspaceService
                .getContentUnits(workspaceUnitKey, properties.getWorkspaceContentBackupDirectory())
                .stream()
                .map(ContentRetrievalBackupUnit::of)
                .toList();
    }

    /**
     * Retrieves object file with the given name and of the given type from the workspace with the given workspace
     * unit key as compressed byte array.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param name given file name.
     * @return retrieved file as compressed byte array.
     * @throws FileUnitRetrievalFailureException if file unit retrieval fails.
     */
    public byte[] getObjectFile(String workspaceUnitKey, String name) throws FileUnitRetrievalFailureException {
        return workspaceService.getContentFile(workspaceUnitKey, properties.getWorkspaceContentObjectDirectory(), name);
    }

    /**
     * Retrieves backup file with the given name from the workspace with the given workspace unit key as compressed
     * byte array.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param name given file name.
     * @return retrieved file as compressed byte array.
     * @throws FileUnitRetrievalFailureException if file unit retrieval fails.
     */
    public byte[] getBackupFile(String workspaceUnitKey, String name) throws FileUnitRetrievalFailureException {
        return workspaceService.getContentFile(workspaceUnitKey, properties.getWorkspaceContentBackupDirectory(), name);
    }

    /**
     * Removes object file with the given name from the workspace with the help of the given workspace unit key.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @throws FileRemovalFailureException if file removal operation failed.
     */
    public void removeObjectFile(String workspaceUnitKey, String name) throws FileRemovalFailureException {
        workspaceService.removeContentFile(workspaceUnitKey, properties.getWorkspaceContentObjectDirectory(), name);
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
