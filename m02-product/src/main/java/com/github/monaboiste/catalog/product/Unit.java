package com.github.monaboiste.catalog.product;

/**
 * Preferred unit of measure for a product type.
 * Making the unit explicit prevents the classic "is this 5 pieces or 5 kilograms?" bug in
 * reports, inventory, and billing.
 */
public record Unit(String symbol) {

    public Unit {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Unit symbol must not be blank");
        }
    }

    public static Unit of(String symbol) {
        return new Unit(symbol);
    }

    /**
     * Dimensionless unit for products tracked as discrete items.
     */
    public static Unit pieces() {
        return new Unit("pcs");
    }

    @Override
    public String toString() {
        return symbol;
    }
}
