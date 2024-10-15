package com.objectstorage.dto;

import com.objectstorage.entity.ConfigEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
/**
 * Represents input for clean external command.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class CleanExternalCommandDto {
    private ConfigEntity config;

    private String location;
}
