package com.objectstorage.service.workspace;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.*;
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
     * @throws WorkspaceUnitDirectoryCreationFailureException if workspace unit directory creation operation failed.
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
     * Writes given input to the given workspace unit directory.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param name                   given file name.
     * @param input                  given file input.
     * @throws RawContentFileWriteFailureException if file cannot be created.
     */
    public void createFile(
            String workspaceUnitDirectory, String name, InputStream input) throws
            RawContentFileWriteFailureException {
        Path directoryPath = Path.of(workspaceUnitDirectory, name);

        File file = new File(directoryPath.toString());

        try {
            FileUtils.copyInputStreamToFile(input, file);
        } catch (IOException e) {
            throw new RawContentFileWriteFailureException(e.getMessage());
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
     * @throws ContentFilesLocationsRetrievalFailureException if content files locations retrieval operation failed. .
     */
    public List<String> getFilesLocations(String workspaceUnitDirectory) throws
            ContentFilesLocationsRetrievalFailureException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(workspaceUnitDirectory))) {
            List<String> result = new ArrayList<>();

            for (Path file : stream) {
                result.add(file.getFileName().toString());
            }

            return result;
        } catch (IOException e) {
            throw new ContentFilesLocationsRetrievalFailureException(e.getMessage());
        }
    }

    /**
     * Retrieves content from the file of the given name with the help of the given workspace unit directory.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param name                   given name of the content file.
     * @return raw content file stream.
     * @throws ContentFileNotFoundException if the raw content file not found.
     */
    public byte[] getFileContent(String workspaceUnitDirectory, String name) throws
            ContentFileNotFoundException {
        Path contentDirectoryPath = Path.of(workspaceUnitDirectory, name);

        try {
            return FileUtils.readFileToByteArray(new File(contentDirectoryPath.toString()));
        } catch (IOException e) {
            throw new ContentFileNotFoundException(e.getMessage());
        }
    }

    /**
     * Retrieves amount of files in the given workspace unit.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @throws ContentFilesAmountRetrievalFailureException if files amount retrieval failed.
     */
    private Integer getFilesAmount(String workspaceUnitDirectory) throws
            ContentFilesAmountRetrievalFailureException {
        try (Stream<Path> stream = Files.list(Path.of(workspaceUnitDirectory))) {
            return (int) stream.count();
        } catch (IOException e) {
            throw new ContentFilesAmountRetrievalFailureException(e.getMessage());
        }
    }

    /**
     * Removes file of the given name in the given workspace unit.
     *
     * @param workspaceUnitDirectory given workspace unit directory.
     * @param name                   given file name.
     * @throws ContentFileRemovalFailureException if earliest file removal operation failed. .
     */
    private void removeFile(String workspaceUnitDirectory, String name) throws
            ContentFileRemovalFailureException {
        try {
            FileSystemUtils.deleteRecursively(Path.of(workspaceUnitDirectory, name));
        } catch (IOException e) {
            throw new ContentFileRemovalFailureException(e.getMessage());
        }
    }












    /**
     * Compresses given file input.
     *
     * @param input         given file input.
     * @return compressed file input.
     * @throws ContentReferenceCreationFailureException if content reference creation failed.
     */
    public byte[] compressFile(byte[] input) throws
            ContentReferenceCreationFailureException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        try (ZipOutputStream writer = new ZipOutputStream(result)) {
            if (isRawContentAvailable(workspaceUnitDirectory, location)) {
                writer.putNextEntry(new ZipEntry(
                        WorkspaceConfigurationHelper.getZipFolderDefinition(properties.getWorkspaceRawContentDirectory())));

                List<String> rawContentLocations =
                        workspaceService.getRawContentFilesLocations(workspaceUnitDirectory, location);

                byte[] rawContent;

                for (String rawContentLocation : rawContentLocations) {
                    writer.putNextEntry(new ZipEntry(
                            Path.of(properties.getWorkspaceRawContentDirectory(), rawContentLocation).toString()));

                    rawContent =
                            workspaceService.getRawContentFile(workspaceUnitDirectory, location, rawContentLocation);

                    writer.write(rawContent);
                }

            }

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
            throw new ContentReferenceCreationFailureException(e.getMessage());
        }

        return result.toByteArray();
    }







    /**
     * Decompresses given file input.
     *
     * @param input         given file input.
     * @return decompressed file input.
     * @throws ContentReferenceCreationFailureException if content reference creation failed.
     */
    public byte[] decompressFile(byte[] input) throws
            ContentReferenceCreationFailureException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        try (ZipOutputStream writer = new ZipOutputStream(result)) {
            if (isRawContentAvailable(workspaceUnitDirectory, location)) {
                writer.putNextEntry(new ZipEntry(
                        WorkspaceConfigurationHelper.getZipFolderDefinition(properties.getWorkspaceRawContentDirectory())));

                List<String> rawContentLocations =
                        workspaceService.getRawContentFilesLocations(workspaceUnitDirectory, location);

                byte[] rawContent;

                for (String rawContentLocation : rawContentLocations) {
                    writer.putNextEntry(new ZipEntry(
                            Path.of(properties.getWorkspaceRawContentDirectory(), rawContentLocation).toString()));

                    rawContent =
                            workspaceService.getRawContentFile(workspaceUnitDirectory, location, rawContentLocation);

                    writer.write(rawContent);
                }

            }

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
            throw new ContentReferenceCreationFailureException(e.getMessage());
        }

        return result.toByteArray();
    }
}
