package com.github.monaboiste.catalog.product;

import java.util.Map;
import java.util.Optional;

/**
 * Immutable identity attributes of a product type.
 *
 * <p><b>Metadata vs Feature:</b> if changing a value means a different product (not just a
 * different variant or booking), it belongs here. Use
 * {@link com.github.monaboiste.catalog.product.feature.ProductFeatureType} for properties the
 * customer or the booking flow can configure.
 *
 * <p>Examples for escape rooms: {@code difficulty}, {@code durationMinutes}, {@code requiresVr}.
 */
public record ProductMetadata(Map<String, String> entries) {

    public static final ProductMetadata EMPTY = new ProductMetadata(Map.of());

    public ProductMetadata {
        entries = Map.copyOf(entries);
    }

    public static ProductMetadata of(Map<String, String> entries) {
        return new ProductMetadata(entries);
    }

    /**
     * Returns the value for the given key, or empty if absent.
     */
    public Optional<String> get(String key) {
        return Optional.ofNullable(entries.get(key));
    }
}
