package com.objectstorage.dto;

import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.Provider;
import com.objectstorage.model.ValidationSecretsApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Represents dto used to represent the earliest temporate content.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class EarliestTemporateContentDto {
    /**
     * Represents content compound units.
     */
    private List<ContentCompoundUnitDto> contentCompoundUnits;

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
