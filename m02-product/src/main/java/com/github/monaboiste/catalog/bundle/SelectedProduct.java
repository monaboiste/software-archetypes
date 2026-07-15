package com.github.monaboiste.catalog.bundle;

import com.github.monaboiste.catalog.product.ProductIdentifier;

/**
 * A product type chosen for inclusion in a package, along with a quantity.
 * Operates at the type (definition) level so that {@link PackageType#validateSelection}
 * can check structural rules without requiring concrete product instances.
 */
public record SelectedProduct(ProductIdentifier productId, int quantity) {

    /**
     * Selects one unit of the given product.
     */
    public static SelectedProduct of(ProductIdentifier productId) {
        return new SelectedProduct(productId, 1);
    }

    public static SelectedProduct of(ProductIdentifier productId, int quantity) {
        return new SelectedProduct(productId, quantity);
    }
}
