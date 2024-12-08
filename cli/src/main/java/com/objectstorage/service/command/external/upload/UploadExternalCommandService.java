package com.objectstorage.service.command.external.download;

import com.objectstorage.entity.PropertiesEntity;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.exception.VersionMismatchException;
import com.objectstorage.model.ContentDownload;
import com.objectstorage.model.VersionInfoResult;
import com.objectstorage.service.client.info.version.VersionInfoClientService;
import com.objectstorage.service.command.common.ICommand;
import com.objectstorage.service.visualization.state.VisualizationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Represents upload external command service.
 */
@Service
public class UploadExternalCommandService implements ICommand<UploadExternalCommandDto> {
    @Autowired
    private PropertiesEntity properties;

    @Autowired
    private VisualizationState visualizationState;

    /**
     * @see ICommand
     */
    @Override
    public void process(UploadExternalCommandDto uploadExternalCommand) throws ApiServerOperationFailureException {
        visualizationState.getLabel().pushNext();

        VersionInfoClientService versionInfoClientService =
                new VersionInfoClientService(uploadExternalCommand.getConfig().getApiServer().getHost());

        VersionInfoResult versionInfoResult = versionInfoClientService.process(null);

        if (!versionInfoResult.getExternalApi().getHash().equals(properties.getGitCommitId())) {
            throw new ApiServerOperationFailureException(new VersionMismatchException().getMessage());
        }

        visualizationState.getLabel().pushNext();

        visualizationState.getLabel().pushNext();

        UploadContentClientService uploadContentClientService =
                new UploadContentClientService(uploadExternalCommand.getConfig().getApiServer().getHost());

        uploadContentClientService.process(null);

        visualizationState.getLabel().pushNext();
    }
}