package com.github.monaboiste.catalog.product;

import java.util.UUID;

/**
 * Stable, domain-meaningful handle for a product type.
 * A thin wrapper so that callers cannot accidentally pass a raw {@code String} where a
 * product identifier is expected.
 */
public record ProductIdentifier(String value) {

    public ProductIdentifier {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ProductIdentifier must not be blank");
        }
    }

    public static ProductIdentifier of(String value) {
        return new ProductIdentifier(value);
    }

    public static ProductIdentifier randomUuid() {
        return new ProductIdentifier(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
