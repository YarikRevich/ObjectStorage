package com.objectstorage.service.vendor.common;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Contains helpful tools used for vendor configuration.
 */
@ApplicationScoped
public class VendorConfigurationHelper {

    /**
     * Checks if given vendor external API is available.
     *
     * @param base given vendor external API base.
     * @return result of the check.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Boolean isVendorAvailable(String base) {
        try {
            InetAddress.getByName(base);
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
