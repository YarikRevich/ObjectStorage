package com.objectstorage.dto;

import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.Provider;
import com.objectstorage.model.ValidationSecretsApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** Represents secrets cache structure. */
@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SecretsCacheDto {
    /**
     * Represents validation secrets compound.
     */
    private List<ValidationSecretsCompoundDto> validationSecretsCompoundDto;
}
