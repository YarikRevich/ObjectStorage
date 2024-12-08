package com.objectstorage.service.command.external.download;

import com.objectstorage.converter.ConfigCredentialsToContentCredentialsConverter;
import com.objectstorage.converter.ConfigProviderToContentProviderConverter;
import com.objectstorage.dto.DownloadExternalCommandDto;
import com.objectstorage.entity.PropertiesEntity;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.exception.VersionMismatchException;
import com.objectstorage.model.ContentDownload;
import com.objectstorage.model.VersionInfoResult;
import com.objectstorage.service.client.content.download.object.DownloadContentObjectClientService;
import com.objectstorage.service.client.info.version.VersionInfoClientService;
import com.objectstorage.service.command.common.ICommand;
import com.objectstorage.service.visualization.state.VisualizationState;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Represents download external command service.
 */
@Service
public class DownloadExternalCommandService implements ICommand<DownloadExternalCommandDto> {
    @Autowired
    private PropertiesEntity properties;

    @Autowired
    private VisualizationState visualizationState;

    /**
     * @see ICommand
     */
    @Override
    public void process(DownloadExternalCommandDto downloadExternalCommand) throws ApiServerOperationFailureException {
        visualizationState.getLabel().pushNext();

        VersionInfoClientService versionInfoClientService =
                new VersionInfoClientService(downloadExternalCommand.getConfig().getApiServer().getHost());

        VersionInfoResult versionInfoResult = versionInfoClientService.process(null);

        if (!versionInfoResult.getExternalApi().getHash().equals(properties.getGitCommitId())) {
            throw new ApiServerOperationFailureException(new VersionMismatchException().getMessage());
        }

        visualizationState.getLabel().pushNext();

        visualizationState.getLabel().pushNext();

        DownloadContentObjectClientService downloadContentClientService =
                new DownloadContentObjectClientService(downloadExternalCommand.getConfig().getApiServer().getHost());

        ContentDownload request = ContentDownload.of(
                downloadExternalCommand.getLocation(),
                ConfigProviderToContentProviderConverter.convert(
                        downloadExternalCommand.getConfig().getService().getProvider()),
                ConfigCredentialsToContentCredentialsConverter.convert(
                        downloadExternalCommand.getConfig().getService().getProvider(),
                        downloadExternalCommand.getConfig().getService().getCredentials()));

        byte[] contentDownloadResult = downloadContentClientService.process(request);

        try {
            FileUtils.writeByteArrayToFile(new File(downloadExternalCommand.getOutputLocation()), contentDownloadResult);
        } catch (IOException e) {
            throw new ApiServerOperationFailureException(e.getMessage());
        }

        visualizationState.getLabel().pushNext();
    }
}