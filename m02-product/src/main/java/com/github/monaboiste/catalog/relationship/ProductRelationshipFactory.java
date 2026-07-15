package com.github.monaboiste.catalog.relationship;

import com.github.monaboiste.catalog.product.ProductIdentifier;

import java.util.List;

/**
 * Creates {@link ProductRelationship} instances after verifying all configured policies.
 * Centralises enforcement of relationship business rules in one place — policies are applied
 * consistently regardless of which part of the application triggers the creation.
 */
public final class ProductRelationshipFactory {

    private final List<ProductRelationshipPolicy> policies;

    public ProductRelationshipFactory(ProductRelationshipPolicy... policies) {
        this.policies = List.of(policies);
    }

    /**
     * Attempts to create the relationship after running all policies.
     *
     * @param from source product
     * @param to   target product
     * @param type the relationship type
     * @return the created relationship
     * @throws IllegalArgumentException if any policy rejects the combination
     */
    public ProductRelationship define(
            ProductIdentifier from,
            ProductIdentifier to,
            ProductRelationshipType type) {

        boolean allPass = policies.stream()
                .allMatch(p -> p.canDefineFor(from, to, type));

        if (!allPass) {
            throw new IllegalArgumentException(
                    "Cannot define relationship %s → %s (%s): rejected by policy"
                            .formatted(from, to, type));
        }

        return ProductRelationship.of(from, to, type);
    }
}
