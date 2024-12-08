package com.objectstorage.service.command.external.clean.object;

import com.objectstorage.converter.ConfigCredentialsToContentCredentialsConverter;
import com.objectstorage.converter.ConfigProviderToContentProviderConverter;
import com.objectstorage.converter.CredentialsConverter;
import com.objectstorage.dto.CleanExternalCommandDto;
import com.objectstorage.dto.ContentCleanupRequestDto;
import com.objectstorage.dto.ProcessedCredentialsDto;
import com.objectstorage.entity.ConfigEntity;
import com.objectstorage.entity.PropertiesEntity;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.exception.CloudCredentialsFileNotFoundException;
import com.objectstorage.exception.CloudCredentialsValidationException;
import com.objectstorage.exception.VersionMismatchException;
import com.objectstorage.model.*;
import com.objectstorage.service.client.content.clean.object.CleanContentObjectClientService;
import com.objectstorage.service.client.info.version.VersionInfoClientService;
import com.objectstorage.service.client.validation.AcquireSecretsClientService;
import com.objectstorage.service.command.common.ICommand;
import com.objectstorage.service.visualization.state.VisualizationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Represents clean external command service. */
@Service
public class CleanObjectExternalCommandService implements ICommand<CleanExternalCommandDto> {
    @Autowired
    private PropertiesEntity properties;

    @Autowired private VisualizationState visualizationState;

    /**
     * @see ICommand
     */
    @Override
    public void process(CleanExternalCommandDto cleanExternalCommand) throws ApiServerOperationFailureException {
        visualizationState.getLabel().pushNext();

        VersionInfoClientService versionInfoClientService =
                new VersionInfoClientService(cleanExternalCommand.getConfig().getApiServer().getHost());

        VersionInfoResult versionInfoResult = versionInfoClientService.process(null);

        if (!versionInfoResult.getExternalApi().getHash().equals(properties.getGitCommitId())) {
            throw new ApiServerOperationFailureException(new VersionMismatchException().getMessage());
        }

        visualizationState.getLabel().pushNext();

        visualizationState.getLabel().pushNext();

        AcquireSecretsClientService acquireSecretsClientService =
                new AcquireSecretsClientService(
                        cleanExternalCommand.getConfig().getApiServer().getHost());

        List<ValidationSecretsUnit> validationSecretsUnits = new ArrayList<>();

        for (ConfigEntity.Service service : cleanExternalCommand.getConfig().getService()) {
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

        CleanContentObjectClientService cleanContentClientService =
                new CleanContentObjectClientService(cleanExternalCommand.getConfig().getApiServer().getHost());

        ContentCleanupRequestDto request = ContentCleanupRequestDto.of(
                validationSecretsApplicationResult.getToken(),
                ContentCleanup.of(
                        cleanExternalCommand.getLocation()));

        cleanContentClientService.process(request);

        visualizationState.getLabel().pushNext();
    }
}