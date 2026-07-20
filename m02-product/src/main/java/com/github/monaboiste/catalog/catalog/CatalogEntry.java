package com.github.monaboiste.catalog.catalog;

import com.github.monaboiste.catalog.applicability.ApplicabilityConstraint;
import com.github.monaboiste.catalog.applicability.ApplicabilityContext;
import com.github.monaboiste.catalog.product.Product;

import java.time.LocalDate;
import java.util.Set;

/**
 * The world of sales — one product as offered in a specific context (city, channel, time window).
 *
 * <p>Separates the <em>world of definition</em> ({@link Product} — what a product IS) from the
 * <em>world of sales</em> (when, where, and under what conditions it is offered). The same
 * {@code ProductType} can back entries in Warsaw, Kraków, and Wrocław simultaneously with
 * different sales constraints. A product offered only in Warsaw simply has no entry in the other
 * cities' catalogs — placement alone expresses availability.
 *
 * <p>Following L08: intrinsic rules (health declaration, legal age) belong on the
 * {@link Product} and are enforced in {@link Product#isApplicableFor}. Sales-context rules
 * (city, channel, day-of-week) belong here on the {@code CatalogEntry.salesConstraint}.
 *
 * <p>Availability check ({@link #isAvailableFor}):
 * <ol>
 *   <li>The date falls within {@link Validity} — entry has not expired.
 *   <li>The product's intrinsic constraint is satisfied (age, health declaration, etc.).
 *   <li>The catalog-level sales constraint is satisfied (city, channel, day-of-week, etc.).
 * </ol>
 */
public record CatalogEntry(
        CatalogEntryId id,
        Product product,
        String displayName,
        Set<String> categories,
        Validity validity,
        ApplicabilityConstraint salesConstraint) {

    public CatalogEntry {
        if (id == null) {
            throw new IllegalArgumentException("CatalogEntryId must not be null");
        }
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        if (categories == null) {
            throw new IllegalArgumentException("categories must not be null");
        }
        if (validity == null) {
            throw new IllegalArgumentException("validity must not be null");
        }
        if (salesConstraint == null) {
            throw new IllegalArgumentException("salesConstraint must not be null");
        }
        categories = Set.copyOf(categories);
    }

    /**
     * Returns {@code true} when this entry is available in the given context on the given date.
     */
    public boolean isAvailableFor(ApplicabilityContext ctx, LocalDate date) {
        return validity.contains(date)
                && product.isApplicableFor(ctx)
                && salesConstraint.isSatisfiedBy(ctx);
    }

    // -------------------------------------------------------------------------
    // Factory helpers
    // -------------------------------------------------------------------------

    /**
     * Entry with no sales-context constraint and no expiry — the product is available anywhere
     * this catalog is offered.
     */
    public static CatalogEntry always(Product product, String displayName, Set<String> categories) {
        return new CatalogEntry(
                CatalogEntryId.newOne(),
                product,
                displayName,
                categories,
                Validity.always(),
                ApplicabilityConstraint.alwaysTrue());
    }

    /**
     * Entry that additionally requires a sales-context condition to be met (e.g. weekend-only,
     * VR equipment available).
     */
    public static CatalogEntry withSalesConstraint(
            Product product,
            String displayName,
            Set<String> categories,
            ApplicabilityConstraint salesConstraint) {
        return new CatalogEntry(
                CatalogEntryId.newOne(),
                product,
                displayName,
                categories,
                Validity.always(),
                salesConstraint);
    }
}
