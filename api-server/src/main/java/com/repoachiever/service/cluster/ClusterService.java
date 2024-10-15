package com.objectstorage.service.cluster;

import com.objectstorage.model.LocationsUnit;
import com.objectstorage.dto.CommandExecutorOutputDto;
import com.objectstorage.entity.common.PropertiesEntity;
import com.objectstorage.exception.*;
import com.objectstorage.service.cluster.common.ClusterConfigurationHelper;
import com.objectstorage.service.cluster.resource.ClusterCommunicationResource;
import com.objectstorage.service.command.cluster.deploy.ClusterDeployCommandService;
import com.objectstorage.service.command.cluster.destroy.ClusterDestroyCommandService;
import com.objectstorage.service.executor.CommandExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service used for cluster deployment management, including distribution process.
 */
@ApplicationScoped
public class ClusterService {
    @Inject
    PropertiesEntity properties;

    @Inject
    ClusterCommunicationResource clusterCommunicationResource;

    @Inject
    CommandExecutorService commandExecutorService;

    /**
     * Perform segregation of the given content locations according to the given segregation limitations.
     *
     * @param locations given content locations.
     * @param separator given content location segregation separator.
     * @return segregated content locations.
     */
    public List<List<LocationsUnit>> performContentLocationsSegregation(List<LocationsUnit> locations, Integer separator) {
        List<List<LocationsUnit>> result = new ArrayList<>();

        List<LocationsUnit> temp = new ArrayList<>();

        Integer counter = 0;

        for (LocationsUnit location : locations) {
            temp.add(location);

            if (counter.equals(separator - 1)) {
                result.add(new ArrayList<>(temp));

                temp.clear();

                counter = 0;
            } else {
                counter++;
            }
        }

        if (!temp.isEmpty()) {
            result.add(new ArrayList<>(temp));
        }

        return result;
    }

    /**
     * Performs deployment of ObjectStorage Cluster allocation.
     *
     * @param name given ObjectStorage Cluster allocation name.
     * @param clusterContext given ObjectStorage Cluster context.
     * @return process identificator of the deployed ObjectStorage Cluster instance.
     * @throws ClusterDeploymentFailureException if deployment operation failed.
     */
    public Integer deploy(String name, String clusterContext) throws ClusterDeploymentFailureException {
        ClusterDeployCommandService clusterDeployCommandService =
                new ClusterDeployCommandService(
                        clusterContext,
                        properties.getClusterBinLocation());

        CommandExecutorOutputDto clusterDeployCommandOutput;

        try {
            clusterDeployCommandOutput =
                    commandExecutorService.executeCommand(clusterDeployCommandService);
        } catch (CommandExecutorException e) {
            throw new ClusterDeploymentFailureException(e.getMessage());
        }

        String clusterDeployCommandErrorOutput = clusterDeployCommandOutput.getErrorOutput();

        if (Objects.nonNull(clusterDeployCommandErrorOutput) && !clusterDeployCommandErrorOutput.isEmpty()) {
            throw new ClusterDeploymentFailureException();
        }

        Integer result = Integer.parseInt(
                clusterDeployCommandOutput.
                        getNormalOutput().
                        replaceAll("\n", ""));

        if (!ClusterConfigurationHelper.waitForStart(() -> {
                    try {
                        if (clusterCommunicationResource.retrieveHealthCheck(name)) {
                            return true;
                        }
                    } catch (ClusterOperationFailureException e) {
                        return false;
                    }

                    return false;
                },
                properties.getCommunicationClusterStartupAwaitFrequency(),
                properties.getCommunicationClusterStartupTimeout())) {
            throw new ClusterDeploymentFailureException(new ClusterApplicationTimeoutException().getMessage());
        }

        return result;
    }

    /**
     * Performs destruction of ObjectStorage Cluster allocation.
     *
     * @param pid given ObjectStorage Cluster allocation process id.
     * @throws ClusterDestructionFailureException if destruction operation failed.
     */
    public void destroy(Integer pid) throws ClusterDestructionFailureException {
        ClusterDestroyCommandService clusterDestroyCommandService = new ClusterDestroyCommandService(pid);

        CommandExecutorOutputDto clusterDestroyCommandOutput;

        try {
            clusterDestroyCommandOutput =
                    commandExecutorService.executeCommand(clusterDestroyCommandService);
        } catch (CommandExecutorException e) {
            throw new ClusterDestructionFailureException(e.getMessage());
        }

        String clusterDestroyCommandErrorOutput = clusterDestroyCommandOutput.getErrorOutput();

        if (Objects.nonNull(clusterDestroyCommandErrorOutput) && !clusterDestroyCommandErrorOutput.isEmpty()) {
            throw new ClusterDestructionFailureException();
        }
    }

    /**
     * Performs recreation of ObjectStorage Cluster allocation.
     *
     * @param pid given process identificator of the allocation ObjectStorage Cluster to be removed.
     * @param name given ObjectStorage Cluster allocation name.
     * @param clusterContext given ObjectStorage Cluster context used for the new allocation.
     * @throws ClusterRecreationFailureException if recreation operation failed.
     */
    public Integer recreate(Integer pid, String name, String clusterContext) throws ClusterRecreationFailureException {
        try {
            destroy(pid);
        } catch (ClusterDestructionFailureException e) {
            throw new ClusterRecreationFailureException(e.getMessage());
        }

        try {
            return deploy(name, clusterContext);
        } catch (ClusterDeploymentFailureException e) {
            throw new ClusterRecreationFailureException(e.getMessage());
        }
    }
}


// TODO: make assignment of random identificators

// TODO: probably move this logic to ClusterService

// TODO: should regenerate topology after each location added