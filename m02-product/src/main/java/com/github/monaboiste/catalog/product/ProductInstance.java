package com.github.monaboiste.catalog.product;

/**
 * A concrete occurrence of a {@link ProductType} in the world — a specific booking, contract,
 * or delivery. Separates "what it is" (the type / definition) from "what actually exists" (the
 * instance / realisation).
 *
 * <p><b>Tracking enforcement:</b> the constructor validates {@link ProductTrackingStrategy} and
 * rejects invalid combinations. An {@link ProductTrackingStrategy#INDIVIDUALLY_TRACKED} type
 * always requires a non-null {@link SerialNumber} — the strategy is not mere metadata, it does
 * real work here.
 *
 * <p>// ponytail: batch and quantity tracking omitted — no escape-room domain need.
 * To add BatchId or Quantity, extend the constructor with the appropriate fields and
 * validation branches in {@link #validateTrackingRequirements}.
 */
public final class ProductInstance {

    private final ProductInstanceId id;
    private final ProductType productType;
    private final SerialNumber serialNumber;

    private ProductInstance(ProductInstanceId id, ProductType productType, SerialNumber serialNumber) {
        if (id == null) {
            throw new IllegalArgumentException("ProductInstanceId must be defined");
        }
        if (productType == null) {
            throw new IllegalArgumentException("ProductType must be defined");
        }
        validateTrackingRequirements(productType, serialNumber);

        this.id = id;
        this.productType = productType;
        this.serialNumber = serialNumber;
    }

    /**
     * Creates a product instance and validates tracking requirements.
     *
     * @param type   the product definition
     * @param serial the booking reference; required when {@code type} is
     *               {@link ProductTrackingStrategy#INDIVIDUALLY_TRACKED}
     * @throws IllegalArgumentException if tracking constraints are violated
     */
    public static ProductInstance of(ProductType type, SerialNumber serial) {
        return new ProductInstance(ProductInstanceId.newOne(), type, serial);
    }

    private static void validateTrackingRequirements(ProductType type, SerialNumber serial) {
        if (type.trackingStrategy() == ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                && serial == null) {
            throw new IllegalArgumentException(
                    "INDIVIDUALLY_TRACKED product '%s' requires a SerialNumber"
                            .formatted(type.name()));
        }
    }

    public ProductInstanceId id() {
        return id;
    }

    public ProductType productType() {
        return productType;
    }

    public SerialNumber serialNumber() {
        return serialNumber;
    }
}
