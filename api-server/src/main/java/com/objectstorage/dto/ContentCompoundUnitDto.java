package com.objectstorage.dto;

import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.Provider;
import com.objectstorage.model.ValidationSecretsUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents content compound unit dto.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class ContentCompoundUnitDto {
    /**
     * Represents root location for internal file system.
     */
    private RepositoryContentUnitDto repositoryContentUnitDto;

    /**
     * Represents provider.
     */
    private Provider provider;

    /**
     * Represents full credentials fields.
     */
    private CredentialsFieldsFull credentialsFieldsFull;
}
