package com.objectstorage.service.workspace;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.exception.FileNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.bind.DatatypeConverter;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.util.FileSystemUtils;

/**
 * Represents local content workspace for different users.
 */
@ApplicationScoped
public class WorkspaceService {
    @Inject
    PropertiesEntity properties;

    /**
     * Creates unit key from the given segments.
     *
     * @param segments given segments to be used for unit key creation.
     * @return created unit key from the given segments.
     */
    @SneakyThrows
    public String createUnitKey(String... segments) {
        MessageDigest md = MessageDigest.getInstance("SHA3-256");
        return DatatypeConverter.printHexBinary(md.digest(String.join(".", segments).getBytes()));
    }

    /**
     * Creates workspace unit with the help of the given key.
     *
     * @param key given workspace unit key.
     * @throws WorkspaceUnitDirectoryCreationFailureException if workspace unit directory creation operation fails.
     */
    public void createUnitDirectory(String key) throws
            WorkspaceUnitDirectoryCreationFailureException {
        Path unitDirectoryPath = Path.of(properties.getWorkspaceDirectory(), key);

        if (Files.notExists(unitDirectoryPath)) {
            try {
                Files.createDirectory(unitDirectoryPath);
            } catch (IOException e) {
                throw new WorkspaceUnitDirectoryCreationFailureException(e.getMessage());
            }
        }
    }

    /**
     * Creates workspace content directory of the given type.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param type given content directory type.
     * @throws WorkspaceContentDirectoryCreationFailureException if workspace content directory creation operation fails.
     */
    public void createContentDirectory(String workspaceUnitDirectory, String type) throws
            WorkspaceContentDirectoryCreationFailureException {
        Path unitDirectoryPath = Path.of(workspaceUnitDirectory, type);

        if (Files.notExists(unitDirectoryPath)) {
            try {
                Files.createDirectory(unitDirectoryPath);
            } catch (IOException e) {
                throw new WorkspaceContentDirectoryCreationFailureException(e.getMessage());
            }
        }
    }

    /**
     * Checks if workspace unit directory exists with the help of the given key.
     *
     * @param key given workspace unit directory.
     * @return result of the check.
     */
    public Boolean isUnitDirectoryExist(String key) {
        return Files.exists(Paths.get(properties.getWorkspaceDirectory(), key));
    }

    /**
     * Checks if workspace unit directory of the given type exists.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param type given content directory type.
     * @return result of the check.
     */
    public Boolean isContentDirectoryExist(String workspaceUnitDirectory, String type) {
        return Files.exists(Paths.get(workspaceUnitDirectory, type));
    }

    /**
     * Retrieves path for the workspace unit with the help of the given key.
     *
     * @param key given workspace unit key.
     * @throws WorkspaceUnitDirectoryNotFoundException if workspace unit with the given name does not
     *                                                 exist.
     */
    public String getUnitDirectory(String key) throws WorkspaceUnitDirectoryNotFoundException {
        Path unitDirectoryPath = Path.of(properties.getWorkspaceDirectory(), key);

        if (Files.notExists(unitDirectoryPath)) {
            throw new WorkspaceUnitDirectoryNotFoundException();
        }

        return unitDirectoryPath.toString();
    }

    /**
     * Removes workspace unit with the help of the given key.
     *
     * @param key given workspace unit key.
     * @throws WorkspaceUnitDirectoryRemovalFailureException if IO operation fails.
     */
    public void removeUnitDirectory(String key) throws WorkspaceUnitDirectoryRemovalFailureException {
        try {
            FileSystemUtils.deleteRecursively(Path.of(properties.getWorkspaceDirectory(), key));
        } catch (IOException e) {
            throw new WorkspaceUnitDirectoryRemovalFailureException(e.getMessage());
        }
    }

    /**
     * Writes given input of the given type to the given workspace unit directory.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param type given file type.
     * @param name                   given file name.
     * @param input                  given file input.
     * @throws FileWriteFailureException if file cannot be created.
     */
    public void createFile(String workspaceUnitDirectory, String type, String name, byte[] input) throws
            FileWriteFailureException {
        Path directoryPath = Path.of(workspaceUnitDirectory, type, name);

        File file = new File(directoryPath.toString());

        try {
            FileUtils.writeByteArrayToFile(file, input);
        } catch (IOException e) {
            throw new FileWriteFailureException(e.getMessage());
        }
    }

    /**
     * Checks if file of the given type exists in the given workspace unit directory.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param type given file type.
     * @param name                   given name of the file.
     * @return result if file exists in the given workspace unit directory.
     */
    public Boolean isFilePresent(String workspaceUnitDirectory, String type, String name) {
        return Files.exists(Paths.get(workspaceUnitDirectory, type, name));
    }

    /**
     * Retrieves content files locations of the given type in the given workspace unit.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param type given file type.
     * @return a list of content locations.
     * @throws FilesLocationsRetrievalFailureException if content files locations retrieval operation fails. .
     */
    public List<String> getFilesLocations(String workspaceUnitDirectory, String type) throws
            FilesLocationsRetrievalFailureException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(workspaceUnitDirectory, type))) {
            List<String> result = new ArrayList<>();

            for (Path file : stream) {
                result.add(file.getFileName().toString());
            }

            return result;
        } catch (IOException e) {
            throw new FilesLocationsRetrievalFailureException(e.getMessage());
        }
    }

    /**
     * Retrieves content from the file of the given name and of the given type with the help of the given workspace
     * unit directory.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param type given file type.
     * @param name                   given name of the content file.
     * @return raw content file stream.
     * @throws FileNotFoundException if the raw content file not found.
     */
    public byte[] getFileContent(String workspaceUnitDirectory, String type, String name) throws
            FileNotFoundException {
        Path contentDirectoryPath = Path.of(workspaceUnitDirectory, type, name);

        try {
            return FileUtils.readFileToByteArray(new File(contentDirectoryPath.toString()));
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    /**
     * Removes file of the given name and of the given type in the given workspace unit.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     *                               @param type given file type.
     * @param name                   given file name.
     * @throws FileRemovalFailureException if file removal operation fails.
     */
    public void removeFile(String workspaceUnitDirectory, String type, String name) throws FileRemovalFailureException {
        try {
            Files.delete(Path.of(workspaceUnitDirectory, type, name));
        } catch (IOException e) {
            throw new FileRemovalFailureException(e.getMessage());
        }
    }

    /**
     * Retrieves amount of files of the given type in the given workspace unit directory.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param type                   given content type.
     * @throws FilesAmountRetrievalFailureException if files amount retrieval failed.
     */
    private Integer getFilesAmount(String workspaceUnitDirectory, String type) throws
            FilesAmountRetrievalFailureException {
        try (Stream<Path> stream = Files.list(Path.of(workspaceUnitDirectory, type))) {
            return (int) stream.count();
        } catch (IOException e) {
            throw new FilesAmountRetrievalFailureException(e.getMessage());
        }
    }

    /**
     * Removes earliest file of the given type in the given workspace unit according to the creation timestamp.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param type                   given file type.
     * @throws FileRemovalFailureException if earliest file removal operation failed. .
     */
    private void removeEarliestFile(String workspaceUnitDirectory, String type) throws FileRemovalFailureException {
        Path target = null;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(workspaceUnitDirectory, type))) {
            Instant earliestTimestamp = null;

            for (Path file : stream) {
                BasicFileAttributes attributes;

                try {
                    attributes = Files.readAttributes(file, BasicFileAttributes.class);
                } catch (IOException e) {
                    throw new FileRemovalFailureException(e.getMessage());
                }

                if (Objects.isNull(earliestTimestamp) ||
                        attributes.creationTime().toInstant().isBefore(earliestTimestamp)) {
                    earliestTimestamp = attributes.creationTime().toInstant();
                    target = file;
                }
            }
        } catch (IOException e) {
            throw new FileRemovalFailureException(e.getMessage());
        }

        if (Objects.nonNull(target)) {
            try {
                FileSystemUtils.deleteRecursively(target);
            } catch (IOException e) {
                throw new FileRemovalFailureException(e.getMessage());
            }
        }
    }

    /**
     * Compresses given file input stream.
     *
     * @param inputStream         given file input stream.
     * @return compressed file input.
     * @throws InputCompressionFailureException if input compression fails.
     */
    public byte[] compressFile(InputStream inputStream) throws
            InputCompressionFailureException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        try (ZipOutputStream writer = new ZipOutputStream(result)) {
            writer.putNextEntry(new ZipEntry(properties.getWorkspaceCompressionFileName()));

            writer.write(inputStream.readAllBytes());

            writer.flush();

            writer.finish();

        } catch (IOException e) {
            throw new InputCompressionFailureException(e.getMessage());
        }

        return result.toByteArray();
    }

    /**
     * Adds new file of the given type to the workspace with the given workspace unit key as the compressed input stream.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param type given content type.
     * @param name             given content name.
     * @param inputStream          given input.
     * @throws FileCreationFailureException if file creation operation failed.
     */
    public void addContentFile(String workspaceUnitKey, String type, String name, InputStream inputStream)
            throws FileCreationFailureException {
        if (!isUnitDirectoryExist(workspaceUnitKey)) {
            try {
                createUnitDirectory(workspaceUnitKey);
            } catch (WorkspaceUnitDirectoryCreationFailureException e) {
                throw new FileCreationFailureException(e.getMessage());
            }
        }

        String workspaceUnitDirectory;

        try {
            workspaceUnitDirectory = getUnitDirectory(workspaceUnitKey);
        } catch (WorkspaceUnitDirectoryNotFoundException e) {
            throw new FileCreationFailureException(e.getMessage());
        }

        if (!isContentDirectoryExist(workspaceUnitDirectory, type)) {
            try {
                createContentDirectory(workspaceUnitDirectory, type);
            } catch (WorkspaceContentDirectoryCreationFailureException e) {
                throw new FileCreationFailureException(e.getMessage());
            }
        }

        if (isFilePresent(workspaceUnitDirectory, type, name)) {
            throw new FileCreationFailureException();
        }

        byte[] content;

        try {
            content = compressFile(inputStream);
        } catch (InputCompressionFailureException e) {
            throw new FileCreationFailureException(e.getMessage());
        }

        try {
            createFile(workspaceUnitDirectory, type, name, content);
        } catch (FileWriteFailureException e) {
            throw new FileCreationFailureException(e.getMessage());
        }
    }

    /**
     * Checks if file with the given name and of the given type exists in the workspace with the given workspace unit key.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param type given file type.
     * @param name given file name.
     * @return result of the check.
     * @throws FileExistenceCheckFailureException if file existence check failed.
     */
    public Boolean isContentFilePresent(String workspaceUnitKey, String type, String name)
            throws FileExistenceCheckFailureException {
        if (isUnitDirectoryExist(workspaceUnitKey)) {
            String workspaceUnitDirectory;

            try {
                workspaceUnitDirectory = getUnitDirectory(workspaceUnitKey);
            } catch (WorkspaceUnitDirectoryNotFoundException e) {
                throw new FileExistenceCheckFailureException(e.getMessage());
            }

            return isFilePresent(workspaceUnitDirectory, type, name);
        }

        return false;
    }

    /**
     * Retrieves file from the workspace with the given workspace unit key as compressed byte array.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param type given file type.
     * @param name given file name.
     * @return retrieved file as compressed byte array.
     * @throws FileUnitRetrievalFailureException if file unit retrieval fails.
     */
    public byte[] getContentFile(String workspaceUnitKey, String type, String name) throws FileUnitRetrievalFailureException {
        if (!isUnitDirectoryExist(workspaceUnitKey)) {
            throw new FileUnitRetrievalFailureException();
        }

        String workspaceUnitDirectory;

        try {
            workspaceUnitDirectory = getUnitDirectory(workspaceUnitKey);
        } catch (WorkspaceUnitDirectoryNotFoundException e) {
            throw new FileUnitRetrievalFailureException(e.getMessage());
        }

        try {
            return getFileContent(workspaceUnitDirectory, type, name);
        } catch (FileNotFoundException e) {
            throw new FileUnitRetrievalFailureException(e.getMessage());
        }
    }

    /**
     * Removes file with the given name and of the given type from the workspace with the help of the given workspace
     * unit key.
     *
     * @param workspaceUnitKey given user workspace unit key.
     * @param type given file type.
     * @throws FileRemovalFailureException if file removal operation failed.
     */
    public void removeContentFile(String workspaceUnitKey, String type, String name) throws FileRemovalFailureException {
        if (isUnitDirectoryExist(workspaceUnitKey)) {
            String workspaceUnitDirectory;

            try {
                workspaceUnitDirectory = getUnitDirectory(workspaceUnitKey);
            } catch (WorkspaceUnitDirectoryNotFoundException e) {
                throw new FileRemovalFailureException(e.getMessage());
            }

            removeFile(workspaceUnitDirectory, type, name);
        }
    }

    /**
     * Retrieves amount of content files of the given type in the given workspace unit.
     *
     * @param workspaceUnitKey given workspace unit key.
     * @param type given file type.
     * @throws FilesAmountRetrievalFailureException if content files amount retrieval failed.
     */
    public Integer getContentFilesAmount(String workspaceUnitKey, String type) throws
            FilesAmountRetrievalFailureException {
        if (!isUnitDirectoryExist(workspaceUnitKey)) {
            throw new FilesAmountRetrievalFailureException();
        }

        String workspaceUnitDirectory;

        try {
            workspaceUnitDirectory = getUnitDirectory(workspaceUnitKey);
        } catch (WorkspaceUnitDirectoryNotFoundException e) {
            throw new FilesAmountRetrievalFailureException(e.getMessage());
        }

        return getFilesAmount(workspaceUnitDirectory, type);
    }

    /**
     * Removes earliest content file of the given type in the given workspace unit according to the creation timestamp.
     *
     * @param workspaceUnitKey given workspace unit key.
     * @param type                   given file type.
     * @throws FileRemovalFailureException if earliest file removal operation failed. .
     */
    public void removeEarliestContentFile(String workspaceUnitKey, String type) throws FileRemovalFailureException {
        if (!isUnitDirectoryExist(workspaceUnitKey)) {
            throw new FileRemovalFailureException();
        }

        String workspaceUnitDirectory;

        try {
            workspaceUnitDirectory = getUnitDirectory(workspaceUnitKey);
        } catch (WorkspaceUnitDirectoryNotFoundException e) {
            throw new FileRemovalFailureException(e.getMessage());
        }

        removeEarliestFile(workspaceUnitDirectory, type);
    }
//
//    /**
//     * Decompresses given file input.
//     *
//     * @param input         given file input.
//     * @return decompressed file content.
//     * @throws InputDecompressionFailureException if input decompression creation fails.
//     */
//    public byte[] decompressFile(byte[] input) throws
//            InputDecompressionFailureException {
//        ZipInputStream reader = new ZipInputStream(new ByteArrayInputStream(input));
//
//        ZipEntry zipEntry;
//
//        try {
//            zipEntry = reader.getNextEntry();
//        } catch (IOException e) {
//            throw new InputDecompressionFailureException(e.getMessage());
//        }
//
//        byte[] content;
//
//        try {
//            content = reader.readAllBytes();
//        } catch (IOException e) {
//            throw new InputDecompressionFailureException(e.getMessage());
//        }
//
//        try {
//            reader.closeEntry();
//        } catch (IOException e) {
//            throw new InputDecompressionFailureException(e.getMessage());
//        }
//
//        try {
//            reader.close();
//        } catch (IOException e) {
//            throw new InputDecompressionFailureException(e.getMessage());
//        }
//
//        return content;
//    }
}
