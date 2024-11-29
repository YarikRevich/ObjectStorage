package com.objectstorage.dto;

import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.Provider;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents repository content application unit.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class RepositoryContentApplicationUnitDto {
    /**
     * Represents root location for internal file system.
     */
    private String root;

    /**
     * Represents provider.
     */
    private Provider provider;

    /**
     * Represents full credentials fields.
     */
    private CredentialsFieldsFull credentials;
}