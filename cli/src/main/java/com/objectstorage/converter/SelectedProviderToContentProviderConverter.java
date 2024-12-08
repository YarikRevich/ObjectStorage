package com.objectstorage.converter;

import com.objectstorage.entity.ConfigEntity;
import com.objectstorage.model.Provider;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents selected provider to ObjectStorage API Server content provider converter.
 */
public class SelectedProviderToContentProviderConverter {

    /**
     * Converts given config provider to content provider.
     *
     * @param selectedProvider given selected provider to be converted.
     * @return converted content provider.
     */
    public static Provider convert(String selectedProvider) {
        return Provider.valueOf(
                Arrays.stream(
                                ConfigEntity.Service.Provider.values())
                        .toList()
                        .stream()
                        .filter(element -> Objects.equals(element.toString(), selectedProvider))
                        .map(Enum::name)
                        .toList()
                        .getFirst());
    }
}
