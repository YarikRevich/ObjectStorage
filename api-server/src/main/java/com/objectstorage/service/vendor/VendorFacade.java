package com.objectstorage.service.vendor;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.SecretsConversionException;
import com.objectstorage.model.CredentialsFieldsExternal;
import com.objectstorage.model.Provider;
import com.objectstorage.model.ValidationSecretsApplicationResult;
import com.objectstorage.service.vendor.s3.S3VendorService;
import com.objectstorage.converter.SecretsConverter;
import com.objectstorage.dto.AWSSecretsDto;
import com.objectstorage.dto.ValidationSecretsResultDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Provides high-level access to cloud vendor operations.
 */
@ApplicationScoped
public class VendorFacade {
    @Inject
    S3VendorService s3VendorService;

    /**
     * Converts given raw credentials according to the selected provider, according to the given provider name.
     *
     * @param provider                 given external provider name.
     * @param credentialsFieldExternal given external credentials.
     * @return converted provider credentials validation.
     * @throws SecretsConversionException if secrets conversion fails or secrets are invalid.
     */
    public Object getExternalCredentialsSecrets(
            Provider provider, CredentialsFieldsExternal credentialsFieldExternal) throws SecretsConversionException {
        return switch (provider) {
            case S3 -> {
                AWSSecretsDto secrets =
                        SecretsConverter.convert(AWSSecretsDto.class, credentialsFieldExternal.getFile());

                AWSCredentialsProvider awsCredentialsProvider =
                        s3VendorService.getAWSCredentialsProvider(secrets);

                if (!s3VendorService.isCallerValid(
                        awsCredentialsProvider, credentialsFieldExternal.getRegion())) {
                    throw new SecretsConversionException();
                }

                yield secrets;
            }
            case GCS -> null;
        };
    }
}
