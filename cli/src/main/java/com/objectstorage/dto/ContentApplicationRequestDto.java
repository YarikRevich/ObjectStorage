package com.objectstorage.dto;

import com.objectstorage.model.ContentApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents content application request details.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class ContentApplicationRequestDto {
    private String authorization;

    private ContentApplication contentApplication;
}
