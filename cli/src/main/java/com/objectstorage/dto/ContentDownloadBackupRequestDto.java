package com.objectstorage.dto;

import com.objectstorage.model.ContentBackupDownload;
import com.objectstorage.model.ContentObjectDownload;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents content download backup request details.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class ContentDownloadBackupRequestDto {
    private String authorization;

    private ContentBackupDownload contentBackupDownload;
}