package com.github.monaboiste.catalog.product.feature;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The full set of feature definitions for a product type. Provides lookup by name and clear
 * separation of mandatory vs. optional features.
 */
public final class ProductFeatureTypes {

    private final List<ProductFeatureTypeDefinition> definitions;

    private ProductFeatureTypes(List<ProductFeatureTypeDefinition> definitions) {
        this.definitions = List.copyOf(definitions);
    }

    public static ProductFeatureTypes empty() {
        return new ProductFeatureTypes(List.of());
    }

    public static ProductFeatureTypes of(List<ProductFeatureTypeDefinition> definitions) {
        return new ProductFeatureTypes(definitions);
    }

    /**
     * Returns the definition for the named feature, or empty if not declared.
     */
    public Optional<ProductFeatureTypeDefinition> get(String featureName) {
        return definitions.stream()
                .filter(d -> d.featureType().name().equals(featureName))
                .findFirst();
    }

    /**
     * Returns the feature type for the given name, or empty if not declared.
     */
    public Optional<ProductFeatureType> getFeatureType(String featureName) {
        return get(featureName).map(ProductFeatureTypeDefinition::featureType);
    }

    public boolean has(String featureName) {
        return get(featureName).isPresent();
    }

    public boolean isMandatory(String featureName) {
        return get(featureName).map(ProductFeatureTypeDefinition::mandatory).orElse(false);
    }

    public Set<ProductFeatureType> mandatoryFeatures() {
        return definitions.stream()
                .filter(ProductFeatureTypeDefinition::mandatory)
                .map(ProductFeatureTypeDefinition::featureType)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<ProductFeatureType> optionalFeatures() {
        return definitions.stream()
                .filter(d -> !d.mandatory())
                .map(ProductFeatureTypeDefinition::featureType)
                .collect(Collectors.toUnmodifiableSet());
    }
}
