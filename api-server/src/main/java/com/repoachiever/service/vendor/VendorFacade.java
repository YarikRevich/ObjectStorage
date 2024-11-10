package com.objectstorage.service.vendor;

import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.model.CredentialsFieldsExternal;
import com.objectstorage.model.Provider;
import com.objectstorage.model.ValidationSecretsApplicationResult;
import com.objectstorage.service.vendor.common.VendorConfigurationHelper;
import com.objectstorage.service.vendor.s3.S3VendorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Provides high-level access to VCS vendor operations.
 */
@ApplicationScoped
public class VendorFacade {
    @Inject
    PropertiesEntity properties;

    @Inject
    VendorConfigurationHelper vendorConfigurationHelper;

    @Inject
    S3VendorService gitGitHubVendorService;

    /**
     * Checks if the given external credentials are valid, according to the given provider name.
     *
     * @param provider                 given external provider name.
     * @param credentialsFieldExternal given external credentials.
     * @return result of the check.
     */
    public Boolean areExternalCredentialsValid(Provider provider, CredentialsFieldsExternal credentialsFieldExternal) {
        return switch (provider) {
            case S3 -> {
//                AWSSecretsDto secrets =
//                        SecretsConverter.convert(AWSSecretsDto.class, validationSecretsApplication.getFile());
//
//                AWSCredentialsProvider awsCredentialsProvider =
//                        AWSVendorService.getAWSCredentialsProvider(secrets);
//
//                awsVendorService.isCallerValid(
//                                awsCredentialsProvider, properties.getAwsDefaultRegion())
                yield false;
            }
            case GCS -> {
                yield false;
            }
        };
    }
}
