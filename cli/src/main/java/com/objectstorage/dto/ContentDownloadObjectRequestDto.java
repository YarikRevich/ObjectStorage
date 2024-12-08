package com.objectstorage.dto;

import com.objectstorage.model.ContentCleanup;
import com.objectstorage.model.ContentObjectDownload;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents content download object request details.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class ContentDownloadObjectRequestDto {
    private String authorization;

    private ContentObjectDownload contentObjectDownload;
}