package com.objectstorage.service.command.external.upload.object;

import com.objectstorage.converter.ConfigCredentialsToContentCredentialsConverter;
import com.objectstorage.converter.ConfigProviderToContentProviderConverter;
import com.objectstorage.converter.CredentialsConverter;
import com.objectstorage.converter.SelectedProviderToContentProviderConverter;
import com.objectstorage.dto.ContentDownloadObjectRequestDto;
import com.objectstorage.dto.ContentUploadObjectRequestDto;
import com.objectstorage.dto.ProcessedCredentialsDto;
import com.objectstorage.dto.UploadObjectExternalCommandDto;
import com.objectstorage.entity.ConfigEntity;
import com.objectstorage.entity.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.model.*;
import com.objectstorage.service.client.content.upload.object.UploadContentObjectClientService;
import com.objectstorage.service.client.info.version.VersionInfoClientService;
import com.objectstorage.service.client.validation.AcquireSecretsClientService;
import com.objectstorage.service.command.common.ICommand;
import com.objectstorage.service.visualization.state.VisualizationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents upload external command service.
 */
@Service
public class UploadObjectExternalCommandService implements ICommand<UploadObjectExternalCommandDto> {
    @Autowired
    private PropertiesEntity properties;

    @Autowired
    private VisualizationState visualizationState;

    /**
     * @see ICommand
     */
    @Override
    public void process(UploadObjectExternalCommandDto uploadObjectExternalCommand)
            throws ApiServerOperationFailureException {
        visualizationState.getLabel().pushNext();

        VersionInfoClientService versionInfoClientService =
                new VersionInfoClientService(uploadObjectExternalCommand.getConfig().getApiServer().getHost());

        VersionInfoResult versionInfoResult = versionInfoClientService.process(null);

        if (!versionInfoResult.getExternalApi().getHash().equals(properties.getGitCommitId())) {
            throw new ApiServerOperationFailureException(new VersionMismatchException().getMessage());
        }

        visualizationState.getLabel().pushNext();

        visualizationState.getLabel().pushNext();

        AcquireSecretsClientService acquireSecretsClientService =
                new AcquireSecretsClientService(
                        uploadObjectExternalCommand.getConfig().getApiServer().getHost());

        List<ValidationSecretsUnit> validationSecretsUnits = new ArrayList<>();

        for (ConfigEntity.Service service : uploadObjectExternalCommand.getConfig().getService()) {
            ConfigEntity.Service.Credentials credentials =
                    CredentialsConverter.convert(
                            service.getCredentials(),
                            ConfigEntity.Service.Credentials.class);
            if (Objects.isNull(credentials)) {
                throw new ApiServerOperationFailureException(
                        new CloudCredentialsValidationException().getMessage());
            }

            Path filePath = Paths.get(credentials.getFile());

            if (Files.notExists(filePath)) {
                throw new ApiServerOperationFailureException(
                        new CloudCredentialsFileNotFoundException().getMessage());
            }

            String content;

            try {
                content = Files.readString(filePath);
            } catch (IOException e) {
                throw new ApiServerOperationFailureException(e.getMessage());
            }

            validationSecretsUnits.add(ValidationSecretsUnit.of(
                    ConfigProviderToContentProviderConverter.convert(service.getProvider()),
                    ConfigCredentialsToContentCredentialsConverter.convert(
                            service.getProvider(),
                            ProcessedCredentialsDto.of(
                                    credentials.getId(),
                                    content,
                                    credentials.getRegion()))));
        }

        ValidationSecretsApplication validationSecretsApplication =
                ValidationSecretsApplication.of(validationSecretsUnits);

        ValidationSecretsApplicationResult validationSecretsApplicationResult =
                acquireSecretsClientService.process(validationSecretsApplication);

        visualizationState.getLabel().pushNext();

        visualizationState.getLabel().pushNext();

        UploadContentObjectClientService uploadContentObjectClientService =
                new UploadContentObjectClientService(
                        uploadObjectExternalCommand.getConfig().getApiServer().getHost());

        Path filePath = Paths.get(uploadObjectExternalCommand.getFile());

        if (Files.notExists(filePath)) {
            throw new ApiServerOperationFailureException(
                    new UploadFileNotFoundException().getMessage());
        }

        ContentUploadObjectRequestDto request = ContentUploadObjectRequestDto.of(
                validationSecretsApplicationResult.getToken(),
                uploadObjectExternalCommand.getLocation(),
                new File(uploadObjectExternalCommand.getFile()));

        uploadContentObjectClientService.process(request);

        visualizationState.getLabel().pushNext();
    }
}