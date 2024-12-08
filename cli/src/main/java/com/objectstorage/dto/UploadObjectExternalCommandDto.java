package com.objectstorage.dto;

import com.objectstorage.entity.ConfigEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents input for upload object external command.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class UploadObjectExternalCommandDto {
    private ConfigEntity config;

    private String location;

    private String file;
}
