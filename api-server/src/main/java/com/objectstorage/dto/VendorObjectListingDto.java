package com.objectstorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents vendor object listing.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class VendorObjectListingDto {
    /**
     * Represent location.
     */
    private String location;

    /**
     * Represents created at timestamp.
     */
    private Long createdAt;
}
