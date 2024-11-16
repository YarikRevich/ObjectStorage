package com.objectstorage.dto;

import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.Provider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

/**
 * Represents repository content unit.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class RepositoryContentUnitDto {
    /**
     * Represents root location for internal file system.
     */
    private String root;

    /**
     * Represents selected service provider.
     */
    private Provider provider;

    /**
     * Represents raw credentials.
     */
    private CredentialsFieldsFull credentials;
}