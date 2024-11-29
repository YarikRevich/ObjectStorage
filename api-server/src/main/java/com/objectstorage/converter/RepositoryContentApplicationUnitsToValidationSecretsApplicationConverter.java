package com.objectstorage.converter;

import com.objectstorage.dto.ContentCompoundUnitDto;
import com.objectstorage.dto.RepositoryContentApplicationUnitDto;
import com.objectstorage.model.ValidationSecretsApplication;
import com.objectstorage.model.ValidationSecretsUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents repository content application units to validation secrets application converter;
 */
public class RepositoryContentApplicationUnitsToValidationSecretsApplicationConverter {

    /**
     * Converts given content compound units to validation secrets application.
     *
     * @param repositoryContentApplicationUnits given repository content application units.
     * @return converted validation secrets application.
     */
    public static ValidationSecretsApplication convert(
            List<RepositoryContentApplicationUnitDto> repositoryContentApplicationUnits) {
        List<ValidationSecretsUnit> validationSecretsUnits = new ArrayList<>();

        repositoryContentApplicationUnits.forEach(
                element -> validationSecretsUnits.add(
                        ValidationSecretsUnit.of(
                                element.getProvider(),
                                element.getCredentials())));

        return ValidationSecretsApplication.of(validationSecretsUnits);
    }
}