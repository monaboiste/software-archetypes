package com.github.monaboiste.catalog.bundle;

import com.github.monaboiste.catalog.product.ProductIdentifier;

import java.util.Set;

/**
 * A named group of interchangeable product options within a package.
 * {@link SelectionRule}s refer to product sets to express constraints such as
 * "pick exactly two of these" or "at least one is required".
 *
 * <p>Example: a "Rooms" set containing all four escape rooms, from which the Team Building
 * package requires exactly two to be selected.
 */
public record ProductSet(String name, Set<ProductIdentifier> products) {

    public ProductSet {
        products = Set.copyOf(products);
    }

    /**
     * Returns {@code true} if the given product belongs to this set.
     */
    public boolean contains(ProductIdentifier id) {
        return products.contains(id);
    }
}
