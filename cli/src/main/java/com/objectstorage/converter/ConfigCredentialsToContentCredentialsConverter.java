package com.objectstorage.converter;

import com.objectstorage.dto.ProcessedCredentialsDto;
import com.objectstorage.entity.ConfigEntity;
import com.objectstorage.model.CredentialsFieldsExternal;
import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.CredentialsFieldsInternal;
import com.objectstorage.model.Provider;

/**
 * Represents config credentials to ObjectStorage API Server content credentials converter.
 */
public class ConfigCredentialsToContentCredentialsConverter<T> {

    /**
     * Converts given config credentials to context exporter.
     *
     * @param provider given config provider.
     * @param configCredentials given config credentials to be converted.
     * @return converted context exporter.
     */
    public static CredentialsFieldsFull convert(
            ConfigEntity.Service.Provider provider, ProcessedCredentialsDto configCredentials) {
        return switch (provider) {
            case S3 -> CredentialsFieldsFull.of(
                    CredentialsFieldsInternal.of(configCredentials.getId()),
                    CredentialsFieldsExternal.of(
                            configCredentials.getFile(), configCredentials.getRegion()));
            case GCS -> CredentialsFieldsFull.of(
                    CredentialsFieldsInternal.of(configCredentials.getId()),
                    CredentialsFieldsExternal.of(configCredentials.getFile(), null));
        };
    }
}