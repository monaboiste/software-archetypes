package com.github.monaboiste.catalog.relationship;

import com.github.monaboiste.catalog.product.ProductIdentifier;

/**
 * Domain policy that guards which relationships may be created.
 * Implementations are composed in {@link ProductRelationshipFactory} so that every relationship
 * is validated consistently in one place.
 */
public interface ProductRelationshipPolicy {

    /**
     * Returns {@code true} if the relationship is permissible under this policy.
     */
    boolean canDefineFor(ProductIdentifier from, ProductIdentifier to, ProductRelationshipType type);
}
