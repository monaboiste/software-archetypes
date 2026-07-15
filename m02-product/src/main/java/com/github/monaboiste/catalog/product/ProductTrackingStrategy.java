package com.github.monaboiste.catalog.product;

/**
 * Determines how product instances are tracked in inventory and fulfilment.
 * The strategy is part of the product's definition and is enforced when creating a
 * {@link ProductInstance}.
 *
 * <p>Escape rooms and add-ons use {@link #INDIVIDUALLY_TRACKED} — every booking is a distinct
 * instance identified by a booking reference (serial number).
 *
 * <p>// ponytail: BATCH_TRACKED and QUANTITY_TRACKED are declared for API completeness;
 * the escape-room domain only uses INDIVIDUALLY_TRACKED. Extend ProductInstance when you need them.
 */
public enum ProductTrackingStrategy {
    /**
     * Only one instance ever exists (e.g. a collector's item or a unique artwork).
     */
    UNIQUE,
    /**
     * Each instance has its own serial number (e.g. a booking reference, a contract ID).
     */
    INDIVIDUALLY_TRACKED,
    /**
     * Instances are grouped by production batch; individual tracking is not required.
     */
    BATCH_TRACKED,
    /**
     * Only total quantity is tracked; individual identity does not matter (e.g. bulk goods).
     */
    QUANTITY_TRACKED
}
