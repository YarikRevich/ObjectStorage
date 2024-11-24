package com.objectstorage.resource;

import com.objectstorage.api.ContentResourceApi;
import com.objectstorage.exception.RootIsNotValidException;
import com.objectstorage.model.*;
import com.objectstorage.resource.common.ResourceConfigurationHelper;
import com.objectstorage.service.processor.ProcessorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;

import java.io.InputStream;

/** Contains implementation of ContentResource. */
@ApplicationScoped
public class ContentResource implements ContentResourceApi {
    @Inject
    ProcessorService processorService;

    @Inject
    ResourceConfigurationHelper resourceConfigurationHelper;

    /**
     * Implementation for declared in OpenAPI configuration v1ContentPost method.
     *
     * @param authorization given authorization header.
     * @return retrieved content result.
     */
    @Override
    @SneakyThrows
    public ContentRetrievalResult v1ContentPost(String authorization) {
        ValidationSecretsApplication validationSecretsApplication =
                resourceConfigurationHelper.getJwtDetails(authorization);

        return processorService.retrieveContent(validationSecretsApplication);
    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentApplyPost method.
     *
     * @param authorization given authorization header.
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

        processorService.apply(contentApplication, validationSecretsApplication);
    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentWithdrawDelete method.
     *
     * @param authorization given authorization header.
     */
    @Override
    @SneakyThrows
    public void v1ContentWithdrawDelete(String authorization) {
        ValidationSecretsApplication validationSecretsApplication =
                resourceConfigurationHelper.getJwtDetails(authorization);

        processorService.withdraw(validationSecretsApplication);
    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentUploadPost method.
     *
     * @param authorization given authorization header.
     * @param location given file location.
     * @param file given input file stream.
     */
    @Override
    @SneakyThrows
    public void v1ContentUploadPost(String authorization, String location, InputStream file) {
        ValidationSecretsApplication validationSecretsApplication =
                resourceConfigurationHelper.getJwtDetails(authorization);

        processorService.upload(location, file, validationSecretsApplication);
    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentDownloadPost method.
     *
     * @param authorization given authorization header.
     * @param contentDownload content download application
     * @return downloaded content result.
     */
    @Override
    @SneakyThrows
    public byte[] v1ContentDownloadPost(String authorization, ContentDownload contentDownload) {
        ValidationSecretsApplication validationSecretsApplication =
                resourceConfigurationHelper.getJwtDetails(authorization);

        ValidationSecretsUnit validationSecretsUnit =
                resourceConfigurationHelper.getConfiguredProvider(
                        contentDownload.getProvider(), validationSecretsApplication);

        return processorService.download(
                contentDownload.getLocation(), validationSecretsUnit, validationSecretsApplication);
    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentCleanDelete method.
     *
     * @param authorization given authorization header.
     * @param contentCleanup content cleanup application.
     */
    @Override
    @SneakyThrows
    public void v1ContentCleanDelete(String authorization, ContentCleanup contentCleanup) {
        ValidationSecretsApplication validationSecretsApplication =
                resourceConfigurationHelper.getJwtDetails(authorization);

        processorService.remove(contentCleanup.getLocation(), validationSecretsApplication);
    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentCleanAllDelete method.
     *
     * @param authorization given authorization header.
     */
    @Override
    @SneakyThrows
    public void v1ContentCleanAllDelete(String authorization) {
        ValidationSecretsApplication validationSecretsApplication =
                resourceConfigurationHelper.getJwtDetails(authorization);

        processorService.removeAll(validationSecretsApplication);
    }
}
