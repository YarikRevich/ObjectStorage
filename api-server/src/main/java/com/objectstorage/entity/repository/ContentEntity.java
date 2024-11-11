package com.objectstorage.entity.repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

/**
 * Represents entity used to describe registered locations.
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class ContentEntity {
    private Integer id;

    private String root;

    private Integer provider;

    private Integer secret;
}