package com.objectstorage.entity.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents entity used to describe added configuration.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class ConfigEntity {
    /**
     * Represents id primary key column.
     */
    private Integer id;

    /**
     * Represents foreign key, which references provider id.
     */
    private Integer provider;

    /**
     * Represents foreign key, which references secret id.
     */
    private Integer secret;

    /**
     * Represents configuration file hash column.
     */
    private String hash;
}
