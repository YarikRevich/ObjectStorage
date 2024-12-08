package com.objectstorage.service.visualization.label.download.object;

import com.objectstorage.dto.VisualizationLabelDto;
import com.objectstorage.entity.PropertiesEntity;
import com.objectstorage.service.visualization.common.IVisualizationLabel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents label set used for download object command service.
 */
@Service
public class DownloadObjectCommandVisualizationLabel implements IVisualizationLabel {
    private final ArrayDeque<VisualizationLabelDto> stepsQueue = new ArrayDeque<>();

    private final ArrayDeque<String> batchQueue = new ArrayDeque<>();

    private final ReentrantLock mutex = new ReentrantLock();

    public DownloadObjectCommandVisualizationLabel(@Autowired PropertiesEntity properties) {
        stepsQueue.addAll(
                List.of(
                        VisualizationLabelDto.of(
                                properties.getProgressVisualizationHealthCheckRequestLabel(), 10),
                        VisualizationLabelDto.of(
                                properties.getProgressVisualizationHealthCheckResponseLabel(), 30),
                        VisualizationLabelDto.of(
                                properties.getProgressVisualizationVersionRequestLabel(), 40),
                        VisualizationLabelDto.of(
                                properties.getProgressVisualizationVersionResponseLabel(), 60),
                        VisualizationLabelDto.of(properties.getProgressVisualizationDownloadObjectRequestLabel(), 70),
                        VisualizationLabelDto.of(
                                properties.getProgressVisualizationDownloadObjectResponseLabel(), 100)));
    }

    /**
     * @see IVisualizationLabel
     */
    @Override
    public Boolean isEmpty() {
        return stepsQueue.isEmpty();
    }

    /**
     * @see IVisualizationLabel
     */
    @Override
    public Boolean isNext() {
        mutex.lock();

        try {
            return !batchQueue.isEmpty();
        } finally {
            mutex.unlock();
        }
    }

    /**
     * @see IVisualizationLabel
     */
    @Override
    public void pushNext() {
        mutex.lock();

        batchQueue.push(stepsQueue.pop().toString());

        mutex.unlock();
    }

    /**
     * @see IVisualizationLabel
     */
    @Override
    public String getCurrent() {
        mutex.lock();

        try {
            return batchQueue.pollLast();
        } finally {
            mutex.unlock();
        }
    }
}
