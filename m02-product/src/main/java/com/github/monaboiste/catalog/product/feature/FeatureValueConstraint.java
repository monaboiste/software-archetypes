package com.github.monaboiste.catalog.product.feature;

import java.util.Set;

/**
 * Guards the semantic validity of a feature value. Each constraint knows its expected
 * {@link FeatureValueType} and rejects values that violate the business rule it encodes —
 * keeping validation in the domain model, not scattered across controllers or SQL filters.
 *
 * <p>// ponytail: DecimalRange, DateRange, Regex, Unconstrained omitted — not needed by the
 * current domain. All follow the same interface; add them when a feature requires them.
 */
public interface FeatureValueConstraint {

    FeatureValueType valueType();

    boolean isValid(Object value);

    String description();

    /**
     * Accepts only values from a closed set of strings.
     * Example: catering variant — "pizza" | "sushi" | "vegetarian".
     */
    record AllowedValues(Set<String> allowed) implements FeatureValueConstraint {

        public AllowedValues {
            allowed = Set.copyOf(allowed);
        }

        @Override
        public FeatureValueType valueType() {
            return FeatureValueType.TEXT;
        }

        @Override
        public boolean isValid(Object value) {
            return value instanceof String s && allowed.contains(s);
        }

        @Override
        public String description() {
            return "one of " + allowed;
        }
    }

    /**
     * Accepts integers within an inclusive {@code [min, max]} range.
     * Example: number of participants in a room — between the room's min and max capacity.
     * Accepts both {@link Integer} and {@link String} inputs (flexible for form/API values).
     */
    record NumericRange(int min, int max) implements FeatureValueConstraint {

        @Override
        public FeatureValueType valueType() {
            return FeatureValueType.INTEGER;
        }

        @Override
        public boolean isValid(Object value) {
            if (value instanceof Integer i) {
                return i >= min && i <= max;
            }
            if (value instanceof String s) {
                try {
                    int i = Integer.parseInt(s);
                    return i >= min && i <= max;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return false;
        }

        @Override
        public String description() {
            return "integer in [%d, %d]".formatted(min, max);
        }
    }
}
