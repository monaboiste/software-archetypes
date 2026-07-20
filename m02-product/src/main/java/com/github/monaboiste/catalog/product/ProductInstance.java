package com.github.monaboiste.catalog.product;

import com.github.monaboiste.catalog.product.feature.ProductFeatureInstance;
import com.github.monaboiste.catalog.product.feature.ProductFeatureType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
 * <p><b>Feature enforcement:</b> every mandatory {@link ProductFeatureType} declared on the type
 * must have a corresponding {@link ProductFeatureInstance} supplied at construction. Individual
 * value validation happens inside {@code ProductFeatureInstance} itself.
 *
 * <p>// ponytail: batch and quantity tracking omitted — no escape-room domain need.
 * To add BatchId or Quantity, extend the constructor with the appropriate fields and
 * validation branches in {@link #validateTrackingRequirements}.
 *
 * <p>// ponytail: no dedicated {@code ProductFeatureInstances} container — features are stored
 * in a plain {@code Map} keyed by feature name. Add a container class when a second caller
 * needs richer querying behaviour.
 */
public final class ProductInstance implements Instance {

    private final ProductInstanceId id;
    private final ProductType productType;
    private final SerialNumber serialNumber;
    // ponytail: Map over List for O(1) lookup by feature name
    private final Map<String, ProductFeatureInstance> features;

    private ProductInstance(
            ProductInstanceId id,
            ProductType productType,
            SerialNumber serialNumber,
            List<ProductFeatureInstance> features) {
        if (id == null) {
            throw new IllegalArgumentException("ProductInstanceId must be defined");
        }
        if (productType == null) {
            throw new IllegalArgumentException("ProductType must be defined");
        }
        validateTrackingRequirements(productType, serialNumber);
        validateMandatoryFeatures(productType, features);

        this.id = id;
        this.productType = productType;
        this.serialNumber = serialNumber;
        this.features = features.stream()
                .collect(Collectors.toUnmodifiableMap(
                        f -> f.featureType().name(),
                        f -> f));
    }

    /**
     * Creates a product instance with feature values and validates tracking + mandatory features.
     *
     * @param type     the product definition
     * @param serial   the booking reference; required when {@code type} is
     *                 {@link ProductTrackingStrategy#INDIVIDUALLY_TRACKED}
     * @param features feature instances for every mandatory feature declared on {@code type};
     *                 optional features may be omitted
     * @throws IllegalArgumentException if tracking constraints or mandatory features are violated
     */
    public static ProductInstance of(
            ProductType type, SerialNumber serial, List<ProductFeatureInstance> features) {
        return new ProductInstance(ProductInstanceId.newOne(), type, serial, features);
    }

    /**
     * Creates a product instance without feature values. Suitable for types with no mandatory
     * features (e.g. {@code PHOTO_VIDEO}, {@code DEDICATED_GM}). Will throw if {@code type}
     * declares mandatory features — use {@link #of(ProductType, SerialNumber, List)} then.
     *
     * @throws IllegalArgumentException if {@code type} has mandatory features
     */
    public static ProductInstance of(ProductType type, SerialNumber serial) {
        return new ProductInstance(ProductInstanceId.newOne(), type, serial, List.of());
    }

    private static void validateTrackingRequirements(ProductType type, SerialNumber serial) {
        if (type.trackingStrategy() == ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                && serial == null) {
            throw new IllegalArgumentException(
                    "INDIVIDUALLY_TRACKED product '%s' requires a SerialNumber"
                            .formatted(type.name()));
        }
    }

    private static void validateMandatoryFeatures(
            ProductType type, List<ProductFeatureInstance> features) {
        Set<ProductFeatureType> mandatory = type.featureTypes().mandatoryFeatures();
        if (mandatory.isEmpty()) {
            return;
        }
        Set<String> provided = features.stream()
                .map(f -> f.featureType().name())
                .collect(Collectors.toUnmodifiableSet());
        for (ProductFeatureType required : mandatory) {
            if (!provided.contains(required.name())) {
                throw new IllegalArgumentException(
                        "Missing mandatory feature '%s' for product '%s'"
                                .formatted(required.name(), type.name()));
            }
        }
    }

    @Override
    public ProductInstanceId id() {
        return id;
    }

    /**
     * Returns the product definition as a {@link Product} — the {@link Instance} contract.
     * Use {@link #productType()} when you need the typed accessor (e.g. to read feature types).
     */
    @Override
    public Product product() {
        return productType;
    }

    /**
     * Typed accessor for the product definition. Prefer this over {@link #product()} when you
     * need {@code ProductType}-specific methods (tracking strategy, feature types).
     */
    public ProductType productType() {
        return productType;
    }

    @Override
    public SerialNumber serialNumber() {
        return serialNumber;
    }

    /**
     * Returns the feature instance for the given feature name, or {@code null} if not provided.
     */
    public ProductFeatureInstance feature(String featureName) {
        return features.get(featureName);
    }

    /**
     * Returns all supplied feature instances, keyed by feature name.
     */
    public Map<String, ProductFeatureInstance> features() {
        return features;
    }
}
