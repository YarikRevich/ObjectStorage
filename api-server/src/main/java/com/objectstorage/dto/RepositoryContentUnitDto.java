package com.objectstorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents repository content unit.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class RepositoryContentUnitDto {
    /**
     * Represents root location for internal file system.
     */
    private String root;
}