package com.objectstorage.service.vendor.common;

import jakarta.xml.bind.DatatypeConverter;
import lombok.SneakyThrows;

import java.security.MessageDigest;

/**
 * Contains helpful tools used for vendor configuration.
 */
public class VendorConfigurationHelper {
    /**
     * Creates hashed bucket name.
     *
     * @param name given raw bucket name.
     * @return created hashed bucket name.
     */
    @SneakyThrows
    public static String createBucketName(String name) {
        MessageDigest md = MessageDigest.getInstance("SHA3-256");
        return DatatypeConverter.printHexBinary(md.digest(name.getBytes()));
    }
}