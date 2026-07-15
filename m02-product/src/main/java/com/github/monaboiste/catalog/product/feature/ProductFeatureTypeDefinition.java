package com.github.monaboiste.catalog.product.feature;

/**
 * Associates a {@link ProductFeatureType} with a mandatory/optional flag.
 * Mandatory features must be provided on every product instance; optional features may be omitted.
 */
public record ProductFeatureTypeDefinition(ProductFeatureType featureType, boolean mandatory) {

    public static ProductFeatureTypeDefinition mandatory(ProductFeatureType featureType) {
        return new ProductFeatureTypeDefinition(featureType, true);
    }

    public static ProductFeatureTypeDefinition optional(ProductFeatureType featureType) {
        return new ProductFeatureTypeDefinition(featureType, false);
    }
}
