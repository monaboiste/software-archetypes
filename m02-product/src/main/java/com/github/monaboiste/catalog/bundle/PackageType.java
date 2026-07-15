package com.github.monaboiste.catalog.bundle;

import com.github.monaboiste.catalog.applicability.ApplicabilityConstraint;
import com.github.monaboiste.catalog.product.Product;
import com.github.monaboiste.catalog.product.ProductDescription;
import com.github.monaboiste.catalog.product.ProductIdentifier;
import com.github.monaboiste.catalog.product.ProductMetadata;
import com.github.monaboiste.catalog.product.ProductName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A composite product that bundles multiple product types under shared selection rules.
 *
 * <p>Implements {@link Product} so that the rest of the system — catalogues, search, and the
 * relationship graph — can treat packages and simple product types uniformly (Composite pattern
 * from GoF). A package can be offered in a catalogue, carry applicability constraints, and be the
 * subject of a product relationship, just like a simple product.
 *
 * <p>Validation is performed at the type level via {@link SelectedProduct}. This keeps the model
 * simple: we do not need a concrete instance to check whether the structural rules of the package
 * are satisfied.
 *
 * <p>Use {@link #builder} to construct instances.
 */
public final class PackageType implements Product {

    private final ProductIdentifier id;
    private final ProductName name;
    private final ProductDescription description;
    private final ProductMetadata metadata;
    private final ApplicabilityConstraint applicabilityConstraint;
    private final PackageStructure structure;

    private PackageType(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.metadata = builder.metadata;
        this.applicabilityConstraint = builder.applicabilityConstraint;
        this.structure = new PackageStructure(builder.productSets, builder.selectionRules);
    }

    public static Builder builder(
            ProductIdentifier id,
            ProductName name,
            ProductDescription description) {
        return new Builder(id, name, description);
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

    @Override
    public ApplicabilityConstraint applicabilityConstraint() {
        return applicabilityConstraint;
    }

    public PackageStructure structure() {
        return structure;
    }

    /**
     * Validates that the provided selection satisfies all structural rules of this package.
     * Use this before persisting or confirming a booking.
     */
    public PackageValidationResult validateSelection(List<SelectedProduct> selection) {
        return structure.validate(selection);
    }

    public static final class Builder {

        private final ProductIdentifier id;
        private final ProductName name;
        private final ProductDescription description;
        private ProductMetadata metadata = ProductMetadata.EMPTY;
        private ApplicabilityConstraint applicabilityConstraint = ApplicabilityConstraint.alwaysTrue();
        private final Map<String, ProductSet> productSets = new HashMap<>();
        private final List<SelectionRule> selectionRules = new ArrayList<>();

        private Builder(ProductIdentifier id, ProductName name, ProductDescription description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public Builder withMetadata(Map<String, String> entries) {
            this.metadata = ProductMetadata.of(entries);
            return this;
        }

        public Builder withApplicabilityConstraint(ApplicabilityConstraint constraint) {
            this.applicabilityConstraint = constraint;
            return this;
        }

        /**
         * Registers a product set (group of interchangeable options) in this package.
         */
        public Builder withProductSet(ProductSet set) {
            this.productSets.put(set.name(), set);
            return this;
        }

        /**
         * Adds a selection rule that every valid configuration of this package must satisfy.
         */
        public Builder withSelectionRule(SelectionRule rule) {
            this.selectionRules.add(rule);
            return this;
        }

        public PackageType build() {
            return new PackageType(this);
        }
    }
}
