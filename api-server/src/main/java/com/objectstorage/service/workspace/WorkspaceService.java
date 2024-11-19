package com.objectstorage.service.workspace;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.exception.FileNotFoundException;
import com.objectstorage.service.workspace.common.WorkspaceConfigurationHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.bind.DatatypeConverter;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
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
     * Checks if workspace unit directory exists with the help of the given key.
     *
     * @param key given workspace unit directory.
     * @return result if workspace unit directory exists with the help of the given key.
     */
    public Boolean isUnitDirectoryExist(String key) {
        return Files.exists(Paths.get(properties.getWorkspaceDirectory(), key));
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
     * Writes given input to the given workspace unit directory.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param name                   given file name.
     * @param input                  given file input.
     * @throws FileWriteFailureException if file cannot be created.
     */
    public void createFile(String workspaceUnitDirectory, String name, byte[] input) throws
            FileWriteFailureException {
        Path directoryPath = Path.of(workspaceUnitDirectory, name);

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
     * @param name                   given name of the file.
     * @return result if file exists in the given workspace unit directory.
     */
    public Boolean isFilePresent(String workspaceUnitDirectory, String name) {
        return Files.exists(
                Paths.get(
                        workspaceUnitDirectory, name));
    }

    /**
     * Retrieves content files locations in the given workspace unit.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @return a list of content locations.
     * @throws FilesLocationsRetrievalFailureException if content files locations retrieval operation fails. .
     */
    public List<String> getFilesLocations(String workspaceUnitDirectory) throws
            FilesLocationsRetrievalFailureException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(workspaceUnitDirectory))) {
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
     * Retrieves content from the file of the given name with the help of the given workspace unit directory.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param name                   given name of the content file.
     * @return raw content file stream.
     * @throws FileNotFoundException if the raw content file not found.
     */
    public byte[] getFileContent(String workspaceUnitDirectory, String name) throws
            FileNotFoundException {
        Path contentDirectoryPath = Path.of(workspaceUnitDirectory, name);

        try {
            return FileUtils.readFileToByteArray(new File(contentDirectoryPath.toString()));
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    /**
     * Retrieves amount of files of the given location.
     *
     * @param location given location.
     * @throws FilesAmountRetrievalFailureException if files amount retrieval fails.
     */
    public Integer getFilesAmount(String location) throws FilesAmountRetrievalFailureException {
        try (Stream<Path> stream = Files.list(Path.of(location))) {
            return (int) stream.count();
        } catch (IOException e) {
            throw new FilesAmountRetrievalFailureException(e.getMessage());
        }
    }

    /**
     * Removes file of the given name in the given workspace unit.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param name                   given file name.
     * @throws FileRemovalFailureException if file removal operation fails.
     */
    public void removeFile(String workspaceUnitDirectory, String name) throws FileRemovalFailureException {
        try {
            Files.delete(Path.of(workspaceUnitDirectory, name));
        } catch (IOException e) {
            throw new FileRemovalFailureException(e.getMessage());
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
            writer.putNextEntry(new ZipEntry("/"));

            writer.write(inputStream.readAllBytes());

            writer.flush();

            writer.finish();

        } catch (IOException e) {
            throw new InputCompressionFailureException(e.getMessage());
        }

        return result.toByteArray();
    }







    /**
     * Decompresses given file input.
     *
     * @param input         given file input.
     * @return decompressed file content.
     * @throws InputDecompressionFailureException if input decompression creation fails.
     */
    public byte[] decompressFile(byte[] input) throws
            InputDecompressionFailureException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        try (ZipOutputStream writer = new ZipOutputStream(result)) {

            if (isAdditionalContentAvailable(workspaceUnitDirectory, location)) {
                writer.putNextEntry(new ZipEntry(
                        WorkspaceConfigurationHelper.getZipFolderDefinition(
                                properties.getWorkspaceAdditionalContentDirectory())));

                List<String> additionalContentLocations =
                        workspaceService.getAdditionalContentFilesLocations(workspaceUnitDirectory, location);

                String rawContent;

                for (String additionalContentLocation : additionalContentLocations) {
                    writer.putNextEntry(new ZipEntry(
                            Path.of(properties.getWorkspaceAdditionalContentDirectory(), additionalContentLocation)
                                    .toString()));


                    rawContent = AdditionalContentFileToJsonConverter.convert(
                            workspaceService.getAdditionalContentFileContent(
                                    workspaceUnitDirectory, location, additionalContentLocation));

                    if (Objects.isNull(rawContent)) {
                        continue;
                    }

                    writer.write(rawContent.getBytes());
                }
            }

            writer.flush();

            writer.finish();

        } catch (IOException e) {
            throw new InputCompressionFailureException(e.getMessage());
        }

        return result.toByteArray();
    }
}
