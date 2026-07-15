package com.github.monaboiste.catalog.bundle;

import java.util.Arrays;
import java.util.List;

/**
 * A rule that must be satisfied by the set of products selected for a package.
 * Rules can express cardinality ("exactly two rooms") and conditional logic
 * ("if room A is selected, add-on B is required").
 *
 * <p>Use the static factories to build common rules; compose with {@link #ifThen} for
 * conditional ones.
 */
public interface SelectionRule {

    boolean isSatisfiedBy(List<SelectedProduct> selection);

    /**
     * Exactly one product from {@code set} must be selected.
     */
    static SelectionRule single(ProductSet set) {
        return new IsSubsetOf(set, 1, 1);
    }

    /**
     * Zero or one product from {@code set} may be selected.
     */
    static SelectionRule optional(ProductSet set) {
        return new IsSubsetOf(set, 0, 1);
    }

    /**
     * At least one product from {@code set} must be selected.
     */
    static SelectionRule required(ProductSet set) {
        return new IsSubsetOf(set, 1, Integer.MAX_VALUE);
    }

    /**
     * Between {@code min} and {@code max} products (inclusive) from {@code set} must be selected.
     */
    static SelectionRule isSubsetOf(ProductSet set, int min, int max) {
        return new IsSubsetOf(set, min, max);
    }

    /**
     * If {@code condition} is satisfied, all {@code thenRules} must also be satisfied.
     * When the condition is not met, the rule is vacuously true.
     */
    static SelectionRule ifThen(SelectionRule condition, SelectionRule... thenRules) {
        return new ConditionalRule(condition, Arrays.asList(thenRules));
    }

    record IsSubsetOf(ProductSet sourceSet, int min, int max) implements SelectionRule {

        @Override
        public boolean isSatisfiedBy(List<SelectedProduct> selection) {
            long count = selection.stream()
                    .filter(s -> sourceSet.contains(s.productId()))
                    .count();
            return count >= min && count <= max;
        }

        @Override
        public String toString() {
            return "isSubsetOf(%s, min=%d, max=%d)".formatted(sourceSet.name(), min, max);
        }
    }

    record ConditionalRule(SelectionRule condition, List<SelectionRule> thenRules)
            implements SelectionRule {

        public ConditionalRule {
            thenRules = List.copyOf(thenRules);
        }

        @Override
        public boolean isSatisfiedBy(List<SelectedProduct> selection) {
            if (!condition.isSatisfiedBy(selection)) {
                return true; // vacuously true when condition is not met
            }
            return thenRules.stream().allMatch(r -> r.isSatisfiedBy(selection));
        }
    }
}
