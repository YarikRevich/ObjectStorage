package com.objectstorage.service.command.external.version;

import com.objectstorage.entity.ConfigEntity;
import com.objectstorage.entity.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.model.VersionInfoResult;
import com.objectstorage.service.client.info.version.VersionInfoClientService;
import com.objectstorage.service.command.common.ICommand;
import com.objectstorage.service.visualization.state.VisualizationState;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Represents version external command service.
 */
@Service
public class VersionExternalCommandService implements ICommand<ConfigEntity> {
    @Autowired
    private PropertiesEntity properties;

    @Autowired
    private VisualizationState visualizationState;

    /**
     * @see ICommand
     */
    public void process(ConfigEntity config) {
        visualizationState.getLabel().pushNext();

        VersionInfoClientService versionInfoClientService =
                new VersionInfoClientService(config.getApiServer().getHost());

        try {
            VersionInfoResult versionInfoResult = versionInfoClientService.process(null);

            visualizationState.addResult(
                    String.format(
                            "API Server version: %s", versionInfoResult.getExternalApi().getHash()));
        } catch (ApiServerOperationFailureException ignored) {
        }

        visualizationState.addResult(
                String.format("Client version: %s", properties.getGitCommitId()));

        visualizationState.getLabel().pushNext();
    }
}
