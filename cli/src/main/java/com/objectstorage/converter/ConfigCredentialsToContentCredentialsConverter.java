package com.objectstorage.converter;

import com.objectstorage.entity.ConfigEntity;
import com.objectstorage.model.CredentialsFieldsExternal;
import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.CredentialsFieldsInternal;
import com.objectstorage.model.Provider;

/**
 * Represents config credentials to ObjectStorage API Server content credentials converter.
 */
public class ConfigCredentialsToContentCredentialsConverter {

    /**
     * Converts given config credentials to context exporter.
     *
     * @param provider given config provider.
     * @param configCredentials given config credentials to be converted.
     * @return converted context exporter.
     */
    public static CredentialsFieldsFull convert(
            ConfigEntity.Service.Provider provider, ConfigEntity.Service.Credentials configCredentials) {
        return switch (provider) {
            case EXPORTER -> null;
            case GIT_GITHUB -> CredentialsFieldsFull.of(
                    CredentialsFieldsInternal.of(configCredentials.getId()),
                    CredentialsFieldsExternal.of(configCredentials.getToken()));
        };
    }
}