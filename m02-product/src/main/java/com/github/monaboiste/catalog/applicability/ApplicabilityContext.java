package com.github.monaboiste.catalog.applicability;

import java.util.Map;
import java.util.Optional;

/**
 * Describes the situation in which a product is being evaluated for applicability.
 * Constraints inspect named string parameters such as {@code "city"}, {@code "age"}, or
 * {@code "dayType"}.
 *
 * <p>The context is intentionally a flat string map — it travels across service boundaries, can
 * be built from HTTP headers, JWT claims, or a booking form, and does not require the product
 * module to know about Party, Session, or any upstream domain.
 *
 * <p>Example:
 * <pre>{@code
 * ApplicabilityContext ctx = ApplicabilityContext.of(Map.of(
 *     "city",           "Warsaw",
 *     "age",            "25",
 *     "hasVrEquipment", "true"
 * ));
 * }</pre>
 */
public record ApplicabilityContext(Map<String, String> parameters) {

    public ApplicabilityContext {
        parameters = Map.copyOf(parameters);
    }

    public static ApplicabilityContext of(Map<String, String> parameters) {
        return new ApplicabilityContext(parameters);
    }

    /**
     * Returns the parameter value, or empty if the key is absent.
     */
    public Optional<String> get(String key) {
        return Optional.ofNullable(parameters.get(key));
    }
}
