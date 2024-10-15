package com.objectstorage.dto;

import com.objectstorage.entity.ConfigEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents input for download external command.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class DownloadExternalCommandDto {
    private ConfigEntity config;

    private String outputLocation;

    private String location;
}
