package com.objectstorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

/**
 * Represents content upload object request details.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class ContentUploadObjectRequestDto {
    private String authorization;

    private String location;

    private File file;
}