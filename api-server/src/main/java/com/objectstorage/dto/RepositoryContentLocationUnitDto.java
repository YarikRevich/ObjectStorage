package com.objectstorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents repository content location unit.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class RepositoryContentLocationUnitDto {
    /**
     * Represents root location for internal file system.
     */
    private String root;
}