package com.github.monaboiste.catalog.product;

/**
 * Free-text description of a product type.
 */
public record ProductDescription(String value) {

    public static ProductDescription of(String value) {
        return new ProductDescription(value == null ? "" : value);
    }

    @Override
    public String toString() {
        return value;
    }
}
