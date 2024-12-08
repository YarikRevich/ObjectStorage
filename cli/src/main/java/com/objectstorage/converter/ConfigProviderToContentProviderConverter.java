package com.objectstorage.converter;

import com.objectstorage.entity.ConfigEntity;
import com.objectstorage.model.Provider;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents config provider to ObjectStorage API Server content provider converter.
 */
public class ConfigProviderToContentProviderConverter {

    /**
     * Converts given config provider to content provider.
     *
     * @param configProvider given config provider to be converted.
     * @return converted content provider.
     */
    public static Provider convert(ConfigEntity.Service.Provider configProvider) {
        return Provider.valueOf(
                Arrays.stream(
                                ConfigEntity.Service.Provider.values())
                        .toList()
                        .stream()
                        .filter(element -> Objects.equals(element.toString(), configProvider.toString()))
                        .map(Enum::name)
                        .toList()
                        .getFirst());
    }
}
