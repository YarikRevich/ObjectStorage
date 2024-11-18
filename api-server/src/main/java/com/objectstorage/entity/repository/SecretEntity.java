package com.objectstorage.entity.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents entity used to describe registered secrets.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class SecretEntity {
    /**
     * Represents id primary key column.
     */
    private Integer id;

    /**
     * Represents session id column.
     */
    private Integer session;

    /**
     * Represents credentials signature column.
     */
    private String credentials;
}
