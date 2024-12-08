package com.objectstorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents processed credentials details.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class ProcessedCredentialsDto {
    private Integer id;

    private String file;

    private String region;
}
