package com.objectstorage.resource;

import com.objectstorage.api.InfoResourceApi;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.CredentialsAreNotValidException;
import com.objectstorage.exception.CredentialsFieldIsNotValidException;
import com.objectstorage.exception.ProviderIsNotAvailableException;
import com.objectstorage.model.TopologyInfoApplication;
import com.objectstorage.model.VersionExternalApiInfoResult;
import com.objectstorage.model.TopologyInfoUnit;
import com.objectstorage.model.VersionInfoResult;
import com.objectstorage.resource.common.ResourceConfigurationHelper;
import com.objectstorage.service.cluster.ClusterService;
import com.objectstorage.service.cluster.facade.ClusterFacade;
import com.objectstorage.service.vendor.VendorFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Objects;

/**
 * Contains implementation of InfoResource.
 */
@ApplicationScoped
public class InfoResource implements InfoResourceApi {
    @Inject
    PropertiesEntity properties;

    @Inject
    ClusterFacade clusterFacade;

    @Inject
    VendorFacade vendorFacade;

    @Inject
    ResourceConfigurationHelper resourceConfigurationHelper;

    /**
     * Implementation for declared in OpenAPI configuration v1InfoVersionGet method.
     *
     * @return version information result.
     */
    @Override
    public VersionInfoResult v1InfoVersionGet() {
        return VersionInfoResult.of(
                VersionExternalApiInfoResult.of(
                        properties.getApplicationVersion(), properties.getGitCommitId()));
    }

    /**
     * Implementation for declared in OpenAPI configuration v1InfoTopologyPost method.
     *
     * @return topology information result.
     */
    @SneakyThrows
    @Override
    public List<TopologyInfoUnit> v1InfoTopologyPost(TopologyInfoApplication topologyInfoApplication) {
        if (Objects.isNull(topologyInfoApplication)) {
            throw new BadRequestException();
        }

        if (!resourceConfigurationHelper.isExternalCredentialsFieldValid(
                topologyInfoApplication.getProvider(),
                topologyInfoApplication.getCredentials().getExternal())) {
            throw new CredentialsFieldIsNotValidException();
        }

        if (!vendorFacade.isVendorAvailable(topologyInfoApplication.getProvider())) {
            throw new ProviderIsNotAvailableException();
        }

        if (!vendorFacade.isExternalCredentialsValid(
                topologyInfoApplication.getProvider(), topologyInfoApplication.getCredentials().getExternal())) {
            throw new CredentialsAreNotValidException();
        }

        return clusterFacade.retrieveTopology(topologyInfoApplication);
    }
}
