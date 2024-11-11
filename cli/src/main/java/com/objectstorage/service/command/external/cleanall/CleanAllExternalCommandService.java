package com.objectstorage.service.command.external.cleanall;

import com.objectstorage.converter.ConfigCredentialsToContentCredentialsConverter;
import com.objectstorage.converter.ConfigProviderToContentProviderConverter;
import com.objectstorage.dto.CleanExternalCommandDto;
import com.objectstorage.entity.ConfigEntity;
import com.objectstorage.entity.PropertiesEntity;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.exception.VersionMismatchException;
import com.objectstorage.model.ContentCleanup;
import com.objectstorage.model.ContentCleanupAll;
import com.objectstorage.model.VersionInfoResult;
import com.objectstorage.service.client.content.clean.CleanContentClientService;
import com.objectstorage.service.client.content.clean.all.CleanAllContentClientService;
import com.objectstorage.service.client.info.version.VersionInfoClientService;
import com.objectstorage.service.command.common.ICommand;
import com.objectstorage.service.visualization.state.VisualizationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Represents cleanall external command service. */
@Service
public class CleanAllExternalCommandService implements ICommand<ConfigEntity> {
    @Autowired
    private PropertiesEntity properties;

    @Autowired private VisualizationState visualizationState;

    /**
     * @see ICommand
     */
    @Override
    public void process(ConfigEntity config) throws ApiServerOperationFailureException {
        visualizationState.getLabel().pushNext();

        VersionInfoClientService versionInfoClientService =
                new VersionInfoClientService(config.getApiServer().getHost());

        VersionInfoResult versionInfoResult = versionInfoClientService.process(null);

        if (!versionInfoResult.getExternalApi().getHash().equals(properties.getGitCommitId())) {
            throw new ApiServerOperationFailureException(new VersionMismatchException().getMessage());
        }

        visualizationState.getLabel().pushNext();

        visualizationState.getLabel().pushNext();

        CleanAllContentClientService cleanAllContentClientService =
                new CleanAllContentClientService(config.getApiServer().getHost());

        ContentCleanupAll request = ContentCleanupAll.of(
                ConfigProviderToContentProviderConverter.convert(
                        config.getService().getProvider()),
                ConfigCredentialsToContentCredentialsConverter.convert(
                        config.getService().getProvider(),
                        config.getService().getCredentials()));

        cleanAllContentClientService.process(request);

        visualizationState.getLabel().pushNext();
    }
}