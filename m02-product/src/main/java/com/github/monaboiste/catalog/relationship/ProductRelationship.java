package com.github.monaboiste.catalog.relationship;

import com.github.monaboiste.catalog.product.ProductIdentifier;

/**
 * A directed, typed relationship between two products.
 *
 * <p>Relationships are independent entities with their own identity — they do not live as fields
 * inside {@link com.github.monaboiste.catalog.product.ProductType}. This keeps ProductType lean
 * and allows a rich graph of dependencies to evolve without touching product definitions.
 *
 * <p>Relationships are directional: "Egyptian Tomb {@code UPGRADABLE_TO} Alcatraz" does not imply
 * the reverse. Even symmetric relationships (e.g. {@code COMPATIBLE_WITH}) should be defined
 * explicitly and consistently.
 */
public record ProductRelationship(
        ProductRelationshipId id,
        ProductIdentifier from,
        ProductIdentifier to,
        ProductRelationshipType type) {

    /**
     * Creates a new relationship with a generated ID. Callers must ensure that
     * domain policies have been checked via {@link ProductRelationshipFactory}.
     */
    static ProductRelationship of(
            ProductIdentifier from,
            ProductIdentifier to,
            ProductRelationshipType type) {
        return new ProductRelationship(ProductRelationshipId.newOne(), from, to, type);
    }
}
