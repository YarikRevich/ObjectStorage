package com.objectstorage.dto;

import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.Provider;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents dto used to temporate content.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class TemporateContentDto {
    /**
     * Represents target provider.
     */
    private Provider provider;

    /**
     * Represents configured credentials.
     */
    private CredentialsFieldsFull secrets;

    /**
     * Represents file location.
     */
    private String location;

    /**
     * Represents file hash.
     */
    private String hash;

    /**
     * Represents created at timestamp.
     */
    private Long createdAt;
}
