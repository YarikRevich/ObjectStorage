package com.objectstorage.dto;

import com.objectstorage.model.LocationsUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents ObjectStorage Cluster allocation.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class ClusterAllocationDto {
    /**
     * Represents name of ObjectStorage Cluster allocation.
     */
    private String name;

    /**
     * Represents lock state of ObjectStorage Cluster allocation.
     */
    @Setter
    private Boolean locked;

    /**
     * Represents workspace unit key used to target ObjectStorage Cluster results.
     */
    private String workspaceUnitKey;

    /**
     * Represents locations selected for ObjectStorage Cluster allocation.
     */
    private List<LocationsUnit> locations;

    /**
     * Represents process identificator of ObjectStorage Cluster allocation.
     */
    private Integer pid;

    /**
     * Represents context used for ObjectStorage Cluster configuration.
     */
    private String context;
}
