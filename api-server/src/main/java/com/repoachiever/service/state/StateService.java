package com.objectstorage.service.state;

import com.objectstorage.dto.ClusterAllocationDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Service used to operate as a collection of application state properties.
 */
public class StateService {
    /**
     * Represents if ObjectStorage API Server application has been started.
     */
    @Getter
    @Setter
    private static Boolean started = false;

    /**
     * Represents ObjectStorage API Server application startup guard.
     */
    @Getter
    private final static CountDownLatch startGuard = new CountDownLatch(1);

    /**
     * Represents ObjectStorage Cluster topology state guard.
     */
    @Getter
    private final static ReentrantLock topologyStateGuard = new ReentrantLock();

    /**
     * Represents ObjectStorage Cluster communication guard.
     */
    @Getter
    private final static ReentrantLock communicationGuard = new ReentrantLock();

    /**
     * Represents a set of all available ObjectStorage Cluster allocations.
     */
    @Getter
    private final static List<ClusterAllocationDto> clusterAllocations = new ArrayList<>();

    /**
     * Retrieves ObjectStorage Cluster allocations with the given name.
     *
     * @param name given name.
     * @return filtered ObjectStorage Cluster allocations according to the given name.
     */
    public static ClusterAllocationDto getClusterAllocationsByName(String name) {
        return clusterAllocations.
                stream()
                .filter(element -> Objects.equals(element.getName(), name))
                .toList()
                .getFirst();
    }

    /**
     * Retrieves ObjectStorage Cluster allocations with the given workspace unit key.
     *
     * @param workspaceUnitKey given workspace unit key.
     * @return filtered ObjectStorage Cluster allocations according to the given workspace unit key.
     */
    public static List<ClusterAllocationDto> getClusterAllocationsByWorkspaceUnitKey(String workspaceUnitKey) {
        return clusterAllocations.
                stream()
                .filter(element -> Objects.equals(element.getWorkspaceUnitKey(), workspaceUnitKey))
                .toList();
    }

    /**
     * Retrieves ObjectStorage Cluster allocation with the given workspace unit key and name.
     *
     * @param workspaceUnitKey given workspace unit key.
     * @param name             given ObjectStorage Cluster allocation name.
     * @return filtered ObjectStorage Cluster allocations according to the given workspace unit key.
     */
    public static ClusterAllocationDto getClusterAllocationByWorkspaceUnitKeyAndName(
            String workspaceUnitKey, String name) {
        List<ClusterAllocationDto> result = clusterAllocations
                .stream()
                .filter(element -> Objects.equals(element.getWorkspaceUnitKey(), workspaceUnitKey) &&
                        Objects.equals(element.getName(), name))
                .toList();

        if (result.isEmpty()) {
            return null;
        }

        return result.getFirst();
    }

    /**
     * Adds new ObjectStorage Cluster allocations.
     *
     * @param allocations given ObjectStorage Cluster allocations.
     */
    public static void addClusterAllocations(List<ClusterAllocationDto> allocations) {
        clusterAllocations.addAll(allocations);
    }

    /**
     * Checks if ObjectStorage Cluster allocations with the given name exists.
     *
     * @param name given ObjectStorage Cluster allocation.
     * @return result of the check.
     */
    public static Boolean isClusterAllocationPresentByName(String name) {
        return clusterAllocations
                .stream()
                .anyMatch(element -> Objects.equals(element.getName(), name));
    }

    /**
     * Removes ObjectStorage Cluster allocations with the given names.
     *
     * @param names given ObjectStorage Cluster allocation names.
     */
    public static void removeClusterAllocationByNames(List<String> names) {
        names.forEach(
                element1 -> clusterAllocations.removeIf(element2 -> Objects.equals(element2.getName(), element1)));
    }
}
