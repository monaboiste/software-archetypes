package com.github.monaboiste.catalog.applicability;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Composable rule that decides whether a product is applicable in an {@link ApplicabilityContext}.
 * Simple leaf constraints ({@code equals}, {@code in}, {@code greaterThan}) can be composed into
 * trees via logical operators ({@code and}, {@code or}, {@code not}).
 *
 * <p>The product itself answers "am I applicable here?" — the rule is not scattered across
 * controllers, SQL filters, or frontend code.
 *
 * <p>// ponytail: LessThan and Between omitted — not needed by the current domain.
 * They follow the same pattern; add as new permitted records when needed.
 *
 * <p>Example:
 * <pre>{@code
 * ApplicabilityConstraint rule = and(
 *     equals("city", "Warsaw"),
 *     equals("hasVrEquipment", "true")
 * );
 * }</pre>
 */
public sealed interface ApplicabilityConstraint
        permits ApplicabilityConstraint.AlwaysTrueConstraint,
        ApplicabilityConstraint.EqualsConstraint,
        ApplicabilityConstraint.InConstraint,
        ApplicabilityConstraint.GreaterThanConstraint,
        ApplicabilityConstraint.AndConstraint,
        ApplicabilityConstraint.OrConstraint,
        ApplicabilityConstraint.NotConstraint {

    boolean isSatisfiedBy(ApplicabilityContext context);

    /**
     * A constraint that is always satisfied — the default when no rule is configured.
     */
    static ApplicabilityConstraint alwaysTrue() {
        return new AlwaysTrueConstraint();
    }

    /**
     * Context parameter must equal {@code expectedValue}.
     * Named {@code eq} (not {@code equals}) to avoid shadowing {@link Object#equals(Object)}
     * at static-import call sites.
     */
    static ApplicabilityConstraint eq(String parameterName, String expectedValue) {
        return new EqualsConstraint(parameterName, expectedValue);
    }

    /**
     * Context parameter must be one of the supplied values.
     */
    static ApplicabilityConstraint in(String parameterName, String... allowedValues) {
        return new InConstraint(parameterName, Set.of(allowedValues));
    }

    /**
     * Context parameter, parsed as an integer, must be strictly greater than {@code threshold}.
     */
    static ApplicabilityConstraint greaterThan(String parameterName, int threshold) {
        return new GreaterThanConstraint(parameterName, threshold);
    }

    /**
     * All supplied constraints must be satisfied.
     */
    static ApplicabilityConstraint and(ApplicabilityConstraint... constraints) {
        return new AndConstraint(Arrays.asList(constraints));
    }

    /**
     * At least one of the supplied constraints must be satisfied.
     */
    static ApplicabilityConstraint or(ApplicabilityConstraint... constraints) {
        return new OrConstraint(Arrays.asList(constraints));
    }

    /**
     * The supplied constraint must NOT be satisfied.
     */
    static ApplicabilityConstraint not(ApplicabilityConstraint constraint) {
        return new NotConstraint(constraint);
    }

    record AlwaysTrueConstraint() implements ApplicabilityConstraint {
        @Override
        public boolean isSatisfiedBy(ApplicabilityContext context) {
            return true;
        }
    }

    record EqualsConstraint(String parameterName, String expectedValue)
            implements ApplicabilityConstraint {
        @Override
        public boolean isSatisfiedBy(ApplicabilityContext context) {
            return context.get(parameterName)
                    .map(expectedValue::equals)
                    .orElse(false);
        }
    }

    record InConstraint(String parameterName, Set<String> allowedValues)
            implements ApplicabilityConstraint {

        public InConstraint {
            allowedValues = Set.copyOf(allowedValues);
        }

        @Override
        public boolean isSatisfiedBy(ApplicabilityContext context) {
            return context.get(parameterName)
                    .map(allowedValues::contains)
                    .orElse(false);
        }
    }

    record GreaterThanConstraint(String parameterName, int threshold)
            implements ApplicabilityConstraint {
        @Override
        public boolean isSatisfiedBy(ApplicabilityContext context) {
            return context.get(parameterName)
                    .map(v -> {
                        try {
                            return Integer.parseInt(v) > threshold;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .orElse(false);
        }
    }

    record AndConstraint(List<ApplicabilityConstraint> constraints)
            implements ApplicabilityConstraint {

        public AndConstraint {
            constraints = List.copyOf(constraints);
        }

        @Override
        public boolean isSatisfiedBy(ApplicabilityContext context) {
            return constraints.stream().allMatch(c -> c.isSatisfiedBy(context));
        }
    }

    record OrConstraint(List<ApplicabilityConstraint> constraints)
            implements ApplicabilityConstraint {

        public OrConstraint {
            constraints = List.copyOf(constraints);
        }

        @Override
        public boolean isSatisfiedBy(ApplicabilityContext context) {
            return constraints.stream().anyMatch(c -> c.isSatisfiedBy(context));
        }
    }

    record NotConstraint(ApplicabilityConstraint inner) implements ApplicabilityConstraint {
        @Override
        public boolean isSatisfiedBy(ApplicabilityContext context) {
            return !inner.isSatisfiedBy(context);
        }
    }
}
