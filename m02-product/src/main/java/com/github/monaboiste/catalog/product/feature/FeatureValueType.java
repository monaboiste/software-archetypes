package com.github.monaboiste.catalog.product.feature;

/**
 * The value type that a product feature can hold.
 *
 * <p>// ponytail: DECIMAL, DATE, BOOLEAN omitted — escape-room features only need TEXT and INTEGER.
 * Add them as new constants with matching {@link FeatureValueConstraint} implementations when a
 * feature requires them (e.g. price, booking date, VR-required flag).
 */
public enum FeatureValueType {
    TEXT,
    INTEGER
}
