package com.objectstorage.converter;

import com.objectstorage.entity.common.ClusterContextEntity;
import com.objectstorage.model.Provider;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents ObjectStorage Cluster content provider to context provider converter.
 */
public class ContentProviderToClusterContextProviderConverter {

    /**
     * Converts given content provider to context provider.
     *
     * @param contentProvider given content provider to be converted.
     * @return converted context provider.
     */
    public static ClusterContextEntity.Service.Provider convert(Provider contentProvider) {
        return ClusterContextEntity.Service.Provider.valueOf(
                Arrays.stream(
                                ClusterContextEntity.Service.Provider.values())
                        .toList()
                        .stream()
                        .filter(element -> Objects.equals(element.toString(), contentProvider.toString()))
                        .map(Enum::name)
                        .toList()
                        .get(0));
    }
}
