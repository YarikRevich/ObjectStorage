package com.objectstorage.entity.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents entity used to describe registered content.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class ContentEntity {
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
     * Represents content root column.
     */
    private String root;
}