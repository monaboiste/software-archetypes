package com.github.monaboiste.catalog.product.feature;

import java.util.Set;

/**
 * Defines what can be configured on a product: the feature's name and the rule that governs
 * acceptable values.
 *
 * <p><b>Feature vs Metadata:</b> a feature is a configurable dimension — changing it produces a
 * different variant or booking, not a different product. If changing the value means a different
 * product (e.g. switching currency defines a separate product), it belongs in
 * {@link com.github.monaboiste.catalog.product.ProductMetadata} instead.
 *
 * <p>Examples for escape rooms:
 * <ul>
 *   <li>{@code participants} — how many guests are booking (configurable within the room's range)
 *   <li>{@code variant} — catering choice: pizza / sushi / vegetarian
 * </ul>
 */
public record ProductFeatureType(String name, FeatureValueConstraint constraint) {

    /**
     * Feature accepting only the supplied values.
     * Example: {@code withAllowedValues("variant", "pizza", "sushi", "vegetarian")}.
     */
    public static ProductFeatureType withAllowedValues(String name, String... allowed) {
        return new ProductFeatureType(name, new FeatureValueConstraint.AllowedValues(Set.of(allowed)));
    }

    /**
     * Feature accepting integers in {@code [min, max]}.
     * Example: {@code withNumericRange("participants", 2, 5)}.
     */
    public static ProductFeatureType withNumericRange(String name, int min, int max) {
        return new ProductFeatureType(name, new FeatureValueConstraint.NumericRange(min, max));
    }

    /**
     * Returns {@code true} if {@code value} satisfies this feature's constraint.
     */
    public boolean isValid(Object value) {
        return constraint.isValid(value);
    }
}
