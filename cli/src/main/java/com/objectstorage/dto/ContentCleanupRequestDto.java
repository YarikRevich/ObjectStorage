package com.objectstorage.dto;

import com.objectstorage.model.ContentCleanup;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents content cleanup request details.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class ContentCleanupRequestDto {
    private String authorization;

    private ContentCleanup contentCleanup;
}