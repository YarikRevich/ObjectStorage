package com.objectstorage.dto;

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
    public ValidationSecretsApplication validationSecretsApplication;

    public List<ValidationSecretsResultDto> validationSecretsResultDto;
}
