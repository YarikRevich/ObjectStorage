package com.objectstorage.dto;

import com.objectstorage.model.Provider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Represents validation secrets result structure, which can hold secrets for different providers. */
@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ValidationSecretsResultDto {
    Provider provider;

    Boolean valid;

    Object secrets;
}
