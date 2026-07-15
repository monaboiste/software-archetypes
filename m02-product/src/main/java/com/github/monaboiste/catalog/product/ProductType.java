package com.github.monaboiste.catalog.product;

import com.github.monaboiste.catalog.applicability.ApplicabilityConstraint;
import com.github.monaboiste.catalog.product.feature.ProductFeatureType;
import com.github.monaboiste.catalog.product.feature.ProductFeatureTypeDefinition;
import com.github.monaboiste.catalog.product.feature.ProductFeatureTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Definition (type) of a product — what it is, how it behaves, and under what conditions it
 * can be offered. Immutable. Separates the definition from a concrete {@link ProductInstance}.
 *
 * <p>Creating a new product type is a data change in the catalogue, not a code change.
 *
 * <p>Use {@link #builder} to construct instances.
 */
public final class ProductType implements Product {

    private final ProductIdentifier id;
    private final ProductName name;
    private final ProductDescription description;
    private final ProductMetadata metadata;
    private final Unit preferredUnit;
    private final ProductTrackingStrategy trackingStrategy;
    private final ProductFeatureTypes featureTypes;
    private final ApplicabilityConstraint applicabilityConstraint;

    private ProductType(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.metadata = builder.metadata;
        this.preferredUnit = builder.preferredUnit;
        this.trackingStrategy = builder.trackingStrategy;
        this.featureTypes = ProductFeatureTypes.of(builder.featureDefinitions);
        this.applicabilityConstraint = builder.applicabilityConstraint;
    }

    /**
     * Entry point for the fluent builder.
     *
     * @param id               stable product identifier
     * @param name             human-readable name
     * @param description      free-text description
     * @param preferredUnit    unit of measure used when tracking instances
     * @param trackingStrategy how instances of this type are tracked
     */
    public static Builder builder(
            ProductIdentifier id,
            ProductName name,
            ProductDescription description,
            Unit preferredUnit,
            ProductTrackingStrategy trackingStrategy) {
        return new Builder(id, name, description, preferredUnit, trackingStrategy);
    }

    @Override
    public ProductIdentifier id() {
        return id;
    }

    @Override
    public ProductName name() {
        return name;
    }

    @Override
    public ProductDescription description() {
        return description;
    }

    @Override
    public ProductMetadata metadata() {
        return metadata;
    }

    public Unit preferredUnit() {
        return preferredUnit;
    }

    public ProductTrackingStrategy trackingStrategy() {
        return trackingStrategy;
    }

    public ProductFeatureTypes featureTypes() {
        return featureTypes;
    }

    @Override
    public ApplicabilityConstraint applicabilityConstraint() {
        return applicabilityConstraint;
    }

    public static final class Builder {

        private final ProductIdentifier id;
        private final ProductName name;
        private final ProductDescription description;
        private final Unit preferredUnit;
        private final ProductTrackingStrategy trackingStrategy;
        private ProductMetadata metadata = ProductMetadata.EMPTY;
        private final List<ProductFeatureTypeDefinition> featureDefinitions = new ArrayList<>();
        private ApplicabilityConstraint applicabilityConstraint = ApplicabilityConstraint.alwaysTrue();

        private Builder(
                ProductIdentifier id,
                ProductName name,
                ProductDescription description,
                Unit preferredUnit,
                ProductTrackingStrategy trackingStrategy) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.preferredUnit = preferredUnit;
            this.trackingStrategy = trackingStrategy;
        }

        /**
         * Adds immutable identity attributes (see {@link ProductMetadata}).
         */
        public Builder withMetadata(Map<String, String> entries) {
            this.metadata = ProductMetadata.of(entries);
            return this;
        }

        /**
         * Adds a feature that must be set on every product instance.
         */
        public Builder withMandatoryFeature(ProductFeatureType featureType) {
            this.featureDefinitions.add(ProductFeatureTypeDefinition.mandatory(featureType));
            return this;
        }

        /**
         * Adds a feature that may be omitted on a product instance.
         */
        public Builder withOptionalFeature(ProductFeatureType featureType) {
            this.featureDefinitions.add(ProductFeatureTypeDefinition.optional(featureType));
            return this;
        }

        /**
         * Sets the applicability rule for this product type.
         * When omitted, the product is always applicable ({@link ApplicabilityConstraint#alwaysTrue()}).
         */
        public Builder withApplicabilityConstraint(ApplicabilityConstraint constraint) {
            this.applicabilityConstraint = constraint;
            return this;
        }

        public ProductType build() {
            return new ProductType(this);
        }
    }
}
