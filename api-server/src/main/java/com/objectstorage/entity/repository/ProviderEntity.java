package com.objectstorage.entity.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents entity used to describe available providers.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class ProviderEntity {
    /**
     * Represents id primary key column.
     */
    private Integer id;

    /**
     * Represents provider name column.
     */
    private String name;
}
