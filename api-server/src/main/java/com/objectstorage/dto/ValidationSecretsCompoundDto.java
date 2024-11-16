package com.objectstorage.dto;

import com.objectstorage.model.CredentialsFieldsFull;
import com.objectstorage.model.Provider;
import com.objectstorage.model.ValidationSecretsApplication;
import com.objectstorage.model.ValidationSecretsUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


/** Represents validation secrets compound structure. */
@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ValidationSecretsCompoundDto {
    /**
     * Represents given secrets validation application unit.
     */
    private ValidationSecretsUnit validationSecretsUnit;

    /**
     * Represents validation secrets result.
     */
    private Object validationSecretsResult;
}
