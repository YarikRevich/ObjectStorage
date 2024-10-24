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
    private String location;

    private Boolean additional;

    private Provider provider;

    private CredentialsFieldsFull credentials;
}