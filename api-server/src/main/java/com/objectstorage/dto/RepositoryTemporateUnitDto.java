package com.objectstorage.dto;

import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.Provider;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents repository temporate unit.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class RepositoryTemporateUnitDto {
    /**
     * Represents file location.
     */
    private String location;

    /**
     * Represents selected service provider.
     */
    private Provider provider;
}