package com.github.monaboiste.catalog.bundle;

import com.github.monaboiste.catalog.product.Instance;

/**
 * An instance chosen for inclusion in a package booking, with a quantity.
 * Bridges the instance world back to the type-level {@link SelectedProduct} that
 * {@link SelectionRule}s understand — by stripping the instance-specific detail
 * (serial number, feature values) and retaining only the product identity and count.
 *
 * <p>Example: a concrete "Mad Scientist booking for 4 people" → {@code SelectedProduct("ROOM_MAD_SCIENTIST_LAB", 1)}.
 */
public record SelectedInstance(Instance instance, int quantity) {

    public SelectedInstance {
        if (instance == null) {
            throw new IllegalArgumentException("instance must not be null");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
    }

    /**
     * Convenience factory — one unit of the given instance.
     */
    public static SelectedInstance of(Instance instance) {
        return new SelectedInstance(instance, 1);
    }

    /**
     * Projects this selection down to its type identity so that {@link PackageType#validateSelection}
     * can check structural rules without needing concrete instance data.
     */
    public SelectedProduct toSelectedProduct() {
        return SelectedProduct.of(instance.product().id(), quantity);
    }
}
