package com.github.monaboiste.catalog.product;

import com.github.monaboiste.catalog.applicability.ApplicabilityConstraint;
import com.github.monaboiste.catalog.applicability.ApplicabilityContext;

/**
 * Composite contract for the product archetype.
 *
 * <p>Both {@link ProductType} and {@link com.github.monaboiste.catalog.bundle.PackageType}
 * implement this interface. The Composite pattern lets catalogues, search, and the relationship
 * graph treat simple products and composite packages uniformly — without knowing which one they
 * are dealing with.
 */
public interface Product {

    ProductIdentifier id();

    ProductName name();

    ProductDescription description();

    ProductMetadata metadata();

    ApplicabilityConstraint applicabilityConstraint();

    /**
     * Returns {@code true} if this product is applicable in the given context.
     * Delegates to the product's {@link ApplicabilityConstraint} so that the product itself can
     * answer "can you sell me right now?" without pushing that decision to a controller or SQL
     * filter.
     */
    default boolean isApplicableFor(ApplicabilityContext context) {
        return applicabilityConstraint().isSatisfiedBy(context);
    }
}
