package com.objectstorage.service.client.common.helper;

/**
 * Represents client configuration helper.
 */
public class ClientConfigurationHelper {
    /**
     * Converts given raw token value to a wrapped format.
     *
     * @param token given raw token value.
     * @return wrapped token.
     */
    public static String getWrappedToken(String token) {
        return String.format("Bearer: %s", token);
    }
}
