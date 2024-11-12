package com.objectstorage.resource;

import com.objectstorage.api.ContentResourceApi;
import com.objectstorage.dto.SecretsCacheDto;
import com.objectstorage.model.*;
import com.objectstorage.repository.facade.RepositoryFacade;
import com.objectstorage.resource.common.ResourceConfigurationHelper;
import com.objectstorage.service.handler.facade.HandlerFacade;
import com.objectstorage.service.vendor.VendorFacade;
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
    HandlerFacade clusterFacade;

    @Inject
    VendorFacade vendorFacade;

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
        SecretsCacheDto secretsCacheDto =
                resourceConfigurationHelper.getJwtDetails(authorization);

//
//        if (!resourceConfigurationHelper.isExternalCredentialsFieldValid(
//                contentRetrievalApplication.getProvider(),
//                contentRetrievalApplication.getCredentials().getExternal())) {
//            throw new CredentialsFieldIsNotValidException();
//        }
//
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
        SecretsCacheDto secretsCacheDto =
                resourceConfigurationHelper.getJwtDetails(authorization);

//
//        if (!resourceConfigurationHelper.isExporterFieldValid(
//                contentApplication.getProvider(), contentApplication.getExporter())) {
//            throw new ExporterFieldIsNotValidException();
//        }
//
//        if (!resourceConfigurationHelper.isExternalCredentialsFieldValid(
//                contentApplication.getProvider(), contentApplication.getCredentials().getExternal())) {
//            throw new CredentialsFieldIsNotValidException();
//        }
//
//        if (!resourceConfigurationHelper.isLocationsDuplicate(
//                contentApplication.getContent().getLocations()
//                        .stream()
//                        .map(LocationsUnit::getName)
//                        .toList())) {
//            throw new LocationsFieldIsNotValidException();
//        }
//
//        if (!resourceConfigurationHelper.areLocationDefinitionsValid(
//                contentApplication.getProvider(),
//                contentApplication.getContent().getLocations()
//                        .stream()
//                        .map(LocationsUnit::getName)
//                        .toList())) {
//            throw new LocationDefinitionsAreNotValidException();
//        }
//
//        if (!vendorFacade.isVendorAvailable(contentApplication.getProvider())) {
//            throw new ProviderIsNotAvailableException();
//        }
//
//        if (!vendorFacade.areExternalCredentialsValid(
//                contentApplication.getProvider(), contentApplication.getCredentials().getExternal())) {
//            throw new CredentialsAreNotValidException();
//        }
//
//        if (!vendorFacade.areLocationsValid(
//                contentApplication.getProvider(),
//                contentApplication.getCredentials().getExternal(),
//                contentApplication.getContent().getLocations()
//                        .stream()
//                        .map(LocationsUnit::getName)
//                        .toList())) {
//            throw new LocationsAreNotValidException();
//        }
//
//        clusterFacade.apply(contentApplication);

//        repositoryFacade.apply(contentApplication);

    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentWithdrawDelete method.
     */
    @Override
    @SneakyThrows
    public void v1ContentWithdrawDelete(String authorization) {
        SecretsCacheDto secretsCacheDto =
                resourceConfigurationHelper.getJwtDetails(authorization);

//
//        if (!resourceConfigurationHelper.isExternalCredentialsFieldValid(
//                contentWithdrawal.getProvider(), contentWithdrawal.getCredentials().getExternal())) {
//            throw new CredentialsFieldIsNotValidException();
//        }
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
        SecretsCacheDto secretsCacheDto =
                resourceConfigurationHelper.getJwtDetails(authorization);

        System.out.println(file.available());
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
        SecretsCacheDto secretsCacheDto =
                resourceConfigurationHelper.getJwtDetails(authorization);
//
//        if (!resourceConfigurationHelper.isExternalCredentialsFieldValid(
//                contentDownload.getProvider(), contentDownload.getCredentials().getExternal())) {
//            throw new CredentialsFieldIsNotValidException();
//        }
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
        SecretsCacheDto secretsCacheDto =
                resourceConfigurationHelper.getJwtDetails(authorization);
//        if (Objects.isNull(contentCleanup)) {
//            throw new BadRequestException();
//        }
//
//        if (!resourceConfigurationHelper.isExternalCredentialsFieldValid(
//                contentCleanup.getProvider(), contentCleanup.getCredentials().getExternal())) {
//            throw new CredentialsFieldIsNotValidException();
//        }
//
//        if (!vendorFacade.isVendorAvailable(contentCleanup.getProvider())) {
//            throw new ProviderIsNotAvailableException();
//        }
//
//        if (!vendorFacade.areExternalCredentialsValid(
//                contentCleanup.getProvider(), contentCleanup.getCredentials().getExternal())) {
//            throw new CredentialsAreNotValidException();
//        }
//
//        clusterFacade.removeContent(contentCleanup);
    }

    /**
     * Implementation for declared in OpenAPI configuration v1ContentCleanAllDelete method.
     *
     */
    @Override
    @SneakyThrows
    public void v1ContentCleanAllDelete(String authorization) {
        SecretsCacheDto secretsCacheDto =
                resourceConfigurationHelper.getJwtDetails(authorization);
//        if (Objects.isNull(contentCleanupAll)) {
//            throw new BadRequestException();
//        }
//
//        if (!resourceConfigurationHelper.isExternalCredentialsFieldValid(
//                contentCleanupAll.getProvider(), contentCleanupAll.getCredentials().getExternal())) {
//            throw new CredentialsFieldIsNotValidException();
//        }
//
//        if (!vendorFacade.isVendorAvailable(contentCleanupAll.getProvider())) {
//            throw new ProviderIsNotAvailableException();
//        }
//
//        if (!vendorFacade.areExternalCredentialsValid(
//                contentCleanupAll.getProvider(), contentCleanupAll.getCredentials().getExternal())) {
//            throw new CredentialsAreNotValidException();
//        }
//
//        clusterFacade.removeAll(contentCleanupAll);
    }
}
