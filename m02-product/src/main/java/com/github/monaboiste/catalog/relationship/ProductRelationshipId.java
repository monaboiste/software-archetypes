package com.github.monaboiste.catalog.relationship;

import java.util.UUID;

/**
 * Technical identifier for a product relationship.
 */
public record ProductRelationshipId(String value) {

    public static ProductRelationshipId newOne() {
        return new ProductRelationshipId(UUID.randomUUID().toString());
    }
}
