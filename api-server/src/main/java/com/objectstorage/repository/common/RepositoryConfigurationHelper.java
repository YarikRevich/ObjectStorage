package com.objectstorage.repository.common;

import com.objectstorage.model.CredentialsFieldsExternal;
import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.CredentialsFieldsInternal;
import com.objectstorage.model.Provider;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.StringJoiner;

/**
 * Contains helpful tools used for repository configuration.
 */
@ApplicationScoped
public class RepositoryConfigurationHelper {
    /**
     * Packs given external credentials into signature.
     *
     * @param values given any amount of values to be used for signature creation.
     * @return packed external credentials signature.
     */
    private String packExternalCredentials(String ...values) {
        StringJoiner result = new StringJoiner("|");

        for (String value : values) {
            result.add(value);
        }

        return result.toString();
    }

    /**
     * Unpack given external credentials signature.
     *
     * @param credentials given credentials to be unpacked.
     * @return unpacked external credentials signature.
     */
    private List<String> unpackExternalCredentials(String credentials) {
        return List.of(credentials.split("\\|"));
    }

    /**
     * Extracts external credentials from the given credentials field as a signature.
     *
     * @param provider given vendor provider.
     * @param credentialsFieldExternal given credentials field.
     * @return extracted external credentials as optional.
     */
    public String getExternalCredentials(
            Provider provider, CredentialsFieldsExternal credentialsFieldExternal) {
        return switch (provider) {
            case S3 -> packExternalCredentials(
                    credentialsFieldExternal.getFile(), credentialsFieldExternal.getRegion());
            case GCS -> packExternalCredentials(credentialsFieldExternal.getFile());
        };
    }

    /**
     * Converts given raw provider to content provider.
     *
     * @param value given raw provider.
     * @return converted content provider.
     */
    public Provider convertRawProviderToContentProvider(String value) {
        return Provider.fromString(value);
    }

    /**
     * Converts given raw secrets to common credentials according to the given provider.
     *
     * @param provider given provider.
     * @param session given session identificator.
     * @param signature given credentials signature.
     * @return converted common credentials.
     */
    public CredentialsFieldsFull convertRawSecretsToContentCredentials(
            Provider provider, Integer session, String signature) {
        List<String> credentials = unpackExternalCredentials(signature);

        return switch (provider) {
            case S3 -> CredentialsFieldsFull.of(
                    CredentialsFieldsInternal.of(session),
                    CredentialsFieldsExternal.of(
                            credentials.get(0),
                            credentials.get(1)));
            case GCS -> CredentialsFieldsFull.of(
                    CredentialsFieldsInternal.of(session),
                    CredentialsFieldsExternal.of(
                            credentials.getFirst(),
                            null));
        };
    }
}
