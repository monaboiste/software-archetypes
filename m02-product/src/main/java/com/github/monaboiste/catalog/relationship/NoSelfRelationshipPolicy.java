package com.github.monaboiste.catalog.relationship;

import com.github.monaboiste.catalog.product.ProductIdentifier;

/**
 * Rejects any relationship where source and target are the same product.
 * A room "upgradable to itself" or "complemented by itself" is a data error that this policy
 * prevents from ever reaching storage.
 */
public final class NoSelfRelationshipPolicy implements ProductRelationshipPolicy {

    @Override
    public boolean canDefineFor(
            ProductIdentifier from,
            ProductIdentifier to,
            ProductRelationshipType type) {
        return !from.equals(to);
    }
}
