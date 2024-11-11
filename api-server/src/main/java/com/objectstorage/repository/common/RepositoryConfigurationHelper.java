package com.objectstorage.repository.common;

import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.CredentialsFieldsInternal;
import com.objectstorage.model.Provider;
import java.util.Optional;

/**
 * Contains helpful tools used for repository configuration.
 */
public class RepositoryConfigurationHelper {
    /**
     * Converts given raw provider to content provider.
     *
     * @param value given raw provider.
     * @return converted content provider.
     */
    public static Provider convertRawProviderToContentProvider(String value) {
        return Provider.fromString(value);
    }

    /**
     * Converts given raw secrets to common credentials according to the given provider.
     *
     * @param provider given provider.
     * @param session given session identificator.
     * @param credentials given raw credentials.
     * @return converted common credentials.
     */
    public static CredentialsFieldsFull convertRawSecretsToContentCredentials(
            Provider provider, Integer session, Optional<String> credentials) {
        return switch (provider) {
//            case S3 -> CredentialsFieldsFull.of(
//                    CredentialsFieldsInternal.of(session),
//                    CredentialsFieldsExternal.of(credentials.get()));
            case S3 -> null;
            case GCS -> null;
        };
    }
}