package com.objectstorage.service.command.common.helper;

import com.objectstorage.entity.ConfigEntity;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents command configuration helper.
 */
public class CommandConfigurationHelper {
    /**
     * Checks if given provider is valid.
     *
     * @param provider given provider value.
     * @return result of the check.
     */
    public static Boolean isProviderValid(String provider) {
        return !Arrays.stream(ConfigEntity.Service.Provider.values())
                .toList()
                .stream()
                .filter(element -> Objects.equals(element.toString(), provider))
                .map(Enum::name)
                .toList()
                .isEmpty();
    }
}
