package com.objectstorage.entity.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents entity used to describe files in temporate storage.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class TemporateEntity {
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
     * Represents file location column.
     */
    private String location;

    /**
     * Represents file hash column.
     */
    private String hash;
}