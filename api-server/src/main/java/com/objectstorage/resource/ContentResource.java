package com.objectstorage.resource;

import com.objectstorage.api.ContentResourceApi;
import com.objectstorage.exception.RootIsNotValidException;
import com.objectstorage.model.*;
import com.objectstorage.repository.facade.RepositoryFacade;
import com.objectstorage.resource.common.ResourceConfigurationHelper;
import com.objectstorage.service.processor.facade.ProcessorFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;

import java.io.InputStream;

/** Contains implementation of ContentResource. */
@ApplicationScoped
public class ContentResource implements ContentResourceApi {
    @Inject
    RepositoryFacade repositoryFacade;

    @Inject
    ProcessorFacade processorFacade;

    @Inject
    ResourceConfigurationHelper resourceConfigurationHelper;

    /**
     * Implementation for declared in OpenAPI configuration v1ContentPost method.
     *
     * @return retrieved content result.
     */
    @Override
    @SneakyThrows
    public ContentRetrievalResult v1ContentPost(String authorization) {
        ValidationSecretsApplication validationSecretsApplication =
                resourceConfigurationHelper.getJwtDetails(authorization);

//        secretsCacheDto.getValidationSecretsCompoundDto().getFirst()
                //        return clusterFacade.retrieveContent(contentRetrievalApplication);

        return null;
    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentApplyPost method.
     *
     * @param contentApplication content configuration application.
     */
    @Override
    @SneakyThrows
    public void v1ContentApplyPost(String authorization, ContentApplication contentApplication) {
        ValidationSecretsApplication validationSecretsApplication =
                resourceConfigurationHelper.getJwtDetails(authorization);

        if (resourceConfigurationHelper.isRootDefinitionValid(contentApplication.getRoot())) {
            throw new RootIsNotValidException();
        }
//        clusterFacade.apply(contentApplication);

//        repositoryFacade.apply(contentApplication);
    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentWithdrawDelete method.
     */
    @Override
    @SneakyThrows
    public void v1ContentWithdrawDelete(String authorization) {
        ValidationSecretsApplication validationSecretsApplication =
                resourceConfigurationHelper.getJwtDetails(authorization);

//
//        clusterFacade.destroy(contentWithdrawal);
//
//        repositoryFacade.destroy(contentWithdrawal);
    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentUploadPost method.
     *
     * @param authorization
     */
    @Override
    @SneakyThrows
    public void v1ContentUploadPost(String authorization, InputStream file) {
        ValidationSecretsApplication validationSecretsApplication =
                resourceConfigurationHelper.getJwtDetails(authorization);
    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentDownloadPost method.
     *
     * @param contentDownload content download application
     * @return downloaded content result.
     */
    @Override
    @SneakyThrows
    public byte[] v1ContentDownloadPost(String authorization, ContentDownload contentDownload) {
        ValidationSecretsApplication validationSecretsApplication =
                resourceConfigurationHelper.getJwtDetails(authorization);
//
//        return clusterFacade.retrieveContentReference(contentDownload);

        return null;
    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentCleanDelete method.
     *
     * @param contentCleanup content cleanup application.
     */
    @Override
    @SneakyThrows
    public void v1ContentCleanDelete(String authorization, ContentCleanup contentCleanup) {
        ValidationSecretsApplication validationSecretsApplication =
                resourceConfigurationHelper.getJwtDetails(authorization);
//        clusterFacade.removeContent(contentCleanup);
    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentCleanAllDelete method.
     *
     */
    @Override
    @SneakyThrows
    public void v1ContentCleanAllDelete(String authorization) {
        ValidationSecretsApplication validationSecretsApplication =
                resourceConfigurationHelper.getJwtDetails(authorization);

//        clusterFacade.removeAll(contentCleanupAll);
    }
}
