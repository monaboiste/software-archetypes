package com.github.monaboiste.catalog.catalog;

import java.util.UUID;

/**
 * Technical identifier of a catalog entry — distinct from the product's
 * {@link com.github.monaboiste.catalog.product.ProductIdentifier}.
 *
 * <p>The same {@code ProductType} can back entries across multiple catalogs (cities, channels,
 * seasons); each appearance gets its own {@code CatalogEntryId}. The product identifier stays
 * stable; the entry id tracks the sales lifecycle.
 */
public record CatalogEntryId(String value) {

    public CatalogEntryId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CatalogEntryId must not be blank");
        }
    }

    public static CatalogEntryId newOne() {
        return new CatalogEntryId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
