package com.objectstorage.service.command.external.apply;

import com.objectstorage.converter.*;
import com.objectstorage.entity.ConfigEntity;
import com.objectstorage.entity.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.model.*;
import com.objectstorage.service.client.content.apply.ApplyContentClientService;
import com.objectstorage.service.client.info.version.VersionInfoClientService;
import com.objectstorage.service.command.common.ICommand;
import com.objectstorage.service.visualization.state.VisualizationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Represents apply external command service. */
@Service
public class ApplyExternalCommandService implements ICommand<ConfigEntity> {
  @Autowired private PropertiesEntity properties;

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

    ApplyContentClientService applyContentClientService =
            new ApplyContentClientService(config.getApiServer().getHost());

//    ConfigProviderToContentProviderConverter.convert(
//            config.getService().getProvider()),
//            ConfigCredentialsToContentCredentialsConverter.convert(
//                    config.getService().getProvider(),
//                    config.getService().getCredentials())
//
    ContentApplication request = ContentApplication.of(config.getContent().getRoot());

    applyContentClientService.process(request);

    visualizationState.getLabel().pushNext();
  }
}
