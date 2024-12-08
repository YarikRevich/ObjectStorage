package com.objectstorage.dto;

import com.objectstorage.entity.ConfigEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents input for download object external command.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class DownloadObjectExternalCommandDto {
    private ConfigEntity config;

    private String provider;

    private String outputLocation;

    private String location;
}
