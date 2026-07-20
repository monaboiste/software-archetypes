package com.github.monaboiste.catalog.product.feature;

/**
 * A concrete value of a feature on a specific product instance — the realization of a
 * {@link ProductFeatureType} promise. Closes the feature loop: the type defines what can be
 * configured; the instance records what was actually chosen.
 *
 * <p>Value validation against the feature's {@link FeatureValueConstraint} happens at
 * construction — the invariant lives in the model, not in controllers or SQL triggers.
 *
 * <p>Example:
 * <pre>{@code
 * // Catering booking — customer chose sushi
 * ProductFeatureInstance.of(
 *     ProductFeatureType.withAllowedValues("variant", "pizza", "sushi", "vegetarian"),
 *     "sushi")
 *
 * // Room booking — 4 participants
 * ProductFeatureInstance.of(
 *     ProductFeatureType.withNumericRange("participants", 2, 5),
 *     4)
 * }</pre>
 */
public record ProductFeatureInstance(ProductFeatureType featureType, Object value) {

    public ProductFeatureInstance {
        if (featureType == null) {
            throw new IllegalArgumentException("featureType must not be null");
        }
        if (!featureType.isValid(value)) {
            throw new IllegalArgumentException(
                    "Value '%s' is not valid for feature '%s' — expected: %s"
                            .formatted(value, featureType.name(), featureType.constraint().description()));
        }
    }

    public static ProductFeatureInstance of(ProductFeatureType featureType, Object value) {
        return new ProductFeatureInstance(featureType, value);
    }
}
