package com.github.monaboiste.catalog.catalog;

import java.time.LocalDate;

/**
 * Inclusive date window {@code [from, to]} during which a catalog entry is active.
 *
 * <p>Use {@link #always()} for entries with no planned expiry. Use {@link #between} for
 * seasonal promotions, limited-time offers, or entries that replace a deprecated one.
 *
 * <p>Example: a Halloween special available only in October:
 * <pre>{@code
 * Validity.between(LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31))
 * }</pre>
 */
public record Validity(LocalDate from, LocalDate to) {

    /** Sentinel — entry is available without a time limit. */
    public static final Validity ALWAYS = new Validity(LocalDate.MIN, LocalDate.MAX);

    public Validity {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Validity dates must not be null");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "Validity 'from' (%s) must not be after 'to' (%s)".formatted(from, to));
        }
    }

    public static Validity always() {
        return ALWAYS;
    }

    public static Validity between(LocalDate from, LocalDate to) {
        return new Validity(from, to);
    }

    /**
     * Returns {@code true} when {@code date} falls within {@code [from, to]}, inclusive.
     */
    public boolean contains(LocalDate date) {
        return !date.isBefore(from) && !date.isAfter(to);
    }
}
