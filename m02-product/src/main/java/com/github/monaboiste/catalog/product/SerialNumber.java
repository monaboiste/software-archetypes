package com.github.monaboiste.catalog.product;

/**
 * Unique serial number identifying a specific product instance.
 * Required when the product type uses {@link ProductTrackingStrategy#INDIVIDUALLY_TRACKED}.
 *
 * <p>For escape rooms this is a booking reference such as {@code "BOOKING-2025-03-15-NOWAK"}.
 */
public record SerialNumber(String value) {

    public SerialNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SerialNumber must not be blank");
        }
    }

    public static SerialNumber of(String value) {
        return new SerialNumber(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
