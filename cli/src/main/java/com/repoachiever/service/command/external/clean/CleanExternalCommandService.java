package com.objectstorage.service.command.external.clean;

import com.objectstorage.converter.ConfigCredentialsToContentCredentialsConverter;
import com.objectstorage.converter.ConfigProviderToContentProviderConverter;
import com.objectstorage.dto.CleanExternalCommandDto;
import com.objectstorage.entity.PropertiesEntity;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.exception.VersionMismatchException;
import com.objectstorage.model.ContentCleanup;
import com.objectstorage.model.VersionInfoResult;
import com.objectstorage.service.client.content.clean.CleanContentClientService;
import com.objectstorage.service.client.info.version.VersionInfoClientService;
import com.objectstorage.service.command.common.ICommand;
import com.objectstorage.service.visualization.state.VisualizationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Represents clean external command service. */
@Service
public class CleanExternalCommandService implements ICommand<CleanExternalCommandDto> {
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

        CleanContentClientService cleanContentClientService =
                new CleanContentClientService(cleanExternalCommand.getConfig().getApiServer().getHost());

        ContentCleanup request = ContentCleanup.of(
                cleanExternalCommand.getLocation(),
                ConfigProviderToContentProviderConverter.convert(
                        cleanExternalCommand.getConfig().getService().getProvider()),
                ConfigCredentialsToContentCredentialsConverter.convert(
                        cleanExternalCommand.getConfig().getService().getProvider(),
                        cleanExternalCommand.getConfig().getService().getCredentials()));

        cleanContentClientService.process(request);

        visualizationState.getLabel().pushNext();
    }
}