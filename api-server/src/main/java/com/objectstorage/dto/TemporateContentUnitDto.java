package com.objectstorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents repository content unit.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class TemporateContentUnitDto {
    /**
     * Represents provider.
     */
    private Integer provider;

    /**
     * Represents se.
     */
    private Integer secret;

    /**
     * Represents file location.
     */
    private String location;

    /**
     * Represents file hash.
     */
    private String hash;

    /**
     * Represents created at timestamp.
     */
    private Long createdAt;
}