package com.github.monaboiste.catalog.bundle;

import com.github.monaboiste.catalog.product.Instance;
import com.github.monaboiste.catalog.product.Product;
import com.github.monaboiste.catalog.product.ProductInstanceId;
import com.github.monaboiste.catalog.product.SerialNumber;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A concrete booking of a {@link PackageType} — the instance side of the Composite.
 *
 * <p>Symmetric to {@link com.github.monaboiste.catalog.product.ProductInstance} for single
 * products: just as a room booking binds a {@code ProductType} + serial + feature values,
 * a Team Building booking binds a {@code PackageType} + serial + a set of sub-instances
 * (the actual rooms/add-ons chosen, each already constructed and validated).
 *
 * <p>Validation flow: the constructor projects the {@link SelectedInstance}s down to
 * {@link SelectedProduct}s (stripping serial numbers / feature values, retaining product ids)
 * and delegates to {@link PackageType#validateSelection}. An invalid selection throws at
 * construction — the invariant lives in the model, not in a service layer.
 */
public final class PackageInstance implements Instance {

    private final ProductInstanceId id;
    private final PackageType packageType;
    private final SerialNumber serialNumber;
    private final List<SelectedInstance> selection;

    private PackageInstance(
            ProductInstanceId id,
            PackageType packageType,
            SerialNumber serialNumber,
            List<SelectedInstance> selection) {
        if (id == null) {
            throw new IllegalArgumentException("ProductInstanceId must be defined");
        }
        if (packageType == null) {
            throw new IllegalArgumentException("PackageType must be defined");
        }
        if (serialNumber == null) {
            throw new IllegalArgumentException(
                    "PackageInstance '%s' requires a SerialNumber (booking reference)"
                            .formatted(packageType.name()));
        }
        if (selection == null) {
            throw new IllegalArgumentException("selection must not be null");
        }

        List<SelectedProduct> selectedProducts = selection.stream()
                .map(SelectedInstance::toSelectedProduct)
                .collect(Collectors.toList());

        PackageValidationResult result = packageType.validateSelection(selectedProducts);
        if (!result.isValid()) {
            throw new IllegalArgumentException(
                    "Invalid selection for package '%s': %s"
                            .formatted(packageType.name(), result.errors()));
        }

        this.id = id;
        this.packageType = packageType;
        this.serialNumber = serialNumber;
        this.selection = List.copyOf(selection);
    }

    /**
     * Creates a package booking and validates the selection against the package's rules.
     *
     * @param type      the package definition
     * @param serial    the booking reference; required for all package bookings
     * @param selection the concrete sub-instances chosen (rooms, add-ons, …)
     * @throws IllegalArgumentException if the selection violates any {@link SelectionRule}
     */
    public static PackageInstance of(
            PackageType type,
            SerialNumber serial,
            List<SelectedInstance> selection) {
        return new PackageInstance(ProductInstanceId.newOne(), type, serial, selection);
    }

    @Override
    public ProductInstanceId id() {
        return id;
    }

    /**
     * Returns the package definition as a {@link Product} — the {@link Instance} contract.
     * Use {@link #packageType()} for the typed accessor.
     */
    @Override
    public Product product() {
        return packageType;
    }

    /**
     * Typed accessor for the package definition.
     */
    public PackageType packageType() {
        return packageType;
    }

    @Override
    public SerialNumber serialNumber() {
        return serialNumber;
    }

    /**
     * The concrete sub-instances that make up this package booking.
     */
    public List<SelectedInstance> selection() {
        return selection;
    }
}
