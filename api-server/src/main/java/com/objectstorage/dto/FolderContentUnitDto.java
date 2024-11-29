package com.objectstorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.InputStream;

/**
 * Represents folder content unit.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class FolderContentUnitDto {
    /**
     * Represents folder entity name.
     */
    private String location;

    /**
     * Represents folder entity content.
     */
    private byte[] content;
}
