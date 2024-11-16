package com.objectstorage.resource;

import com.objectstorage.api.InfoResourceApi;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.model.VersionExternalApiInfoResult;
import com.objectstorage.model.VersionInfoResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Contains implementation of InfoResource.
 */
@ApplicationScoped
public class InfoResource implements InfoResourceApi {
    @Inject
    PropertiesEntity properties;

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
}
