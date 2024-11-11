package com.objectstorage.converter;

import com.objectstorage.entity.ConfigEntity;
import com.objectstorage.model.ContentUnit;
import com.objectstorage.model.LocationsUnit;

import java.util.List;

/**
 * Represents config locations to ObjectStorage API Server content locations converter.
 */
public class ConfigLocationsToContentLocationsConverter {

    /**
     * Converts given config locations to content locations.
     *
     * @param configLocations given config locations to be converted.
     * @return converted content locations.
     */
    public static ContentUnit convert(List<ConfigEntity.Content.Location> configLocations) {
        return ContentUnit.of(configLocations
                .stream()
                .map(element -> LocationsUnit.of(
                        element.getName(), element.getAdditional()))
                .toList());
    }
}
