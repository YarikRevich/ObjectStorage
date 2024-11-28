package com.objectstorage.converter;

import com.objectstorage.dto.ContentCompoundUnitDto;
import com.objectstorage.model.ValidationSecretsApplication;
import com.objectstorage.model.ValidationSecretsUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents content compounds units to validation secrets application converter;
 */
public class ContentCompoundUnitToValidationSecretsApplicationConverter {

    /**
     * Converts given content compound units to validation secrets application.
     *
     * @param contentCompoundUnits given content compounds units.
     * @return converted validation secrets application.
     */
    public static ValidationSecretsApplication convert(List<ContentCompoundUnitDto> contentCompoundUnits) {
        List<ValidationSecretsUnit> validationSecretsUnits = new ArrayList<>();

        contentCompoundUnits.forEach(
                element -> validationSecretsUnits.add(
                        ValidationSecretsUnit.of(
                                element.getProvider(),
                                element.getCredentialsFieldsFull())));

        return ValidationSecretsApplication.of(validationSecretsUnits);
    }
}