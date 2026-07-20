package com.github.monaboiste.catalog.product;

/**
 * Composite contract for the instance side of the product archetype.
 *
 * <p>Symmetric to the definition-side {@link Product} interface: just as {@link ProductType} and
 * {@link com.github.monaboiste.catalog.bundle.PackageType} are both {@code Product}s, a concrete
 * booking of a single room and a composite "Team Building" booking are both {@code Instance}s.
 * The rest of the system (fulfilment, billing, audit) can treat them uniformly.
 *
 * <p>// ponytail: id stays typed as {@link ProductInstanceId} — renaming to {@code InstanceId}
 * is pure churn with zero behaviour change. Revisit only if the two instance types diverge in
 * identity requirements.
 */
public interface Instance {

    /**
     * System-generated unique identifier for this occurrence (distinct from the booking serial).
     */
    ProductInstanceId id();

    /**
     * The definition this instance realises — a {@link ProductType} for single-product bookings,
     * a {@link com.github.monaboiste.catalog.bundle.PackageType} for package bookings.
     */
    Product product();

    /**
     * Business-meaningful booking reference (e.g. "TB-2025-001"). May be {@code null} for
     * non-tracked types, but package bookings always require one.
     */
    SerialNumber serialNumber();
}
