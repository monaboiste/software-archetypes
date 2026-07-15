package com.github.monaboiste.catalog.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Structural definition of a package: which product sets it contains and which selection rules
 * must be satisfied by a valid configuration. Delegates validation to the rules.
 */
public final class PackageStructure {

    private final Map<String, ProductSet> productSets;
    private final List<SelectionRule> selectionRules;

    public PackageStructure(Map<String, ProductSet> productSets, List<SelectionRule> selectionRules) {
        this.productSets = Map.copyOf(productSets);
        this.selectionRules = List.copyOf(selectionRules);
    }

    public Map<String, ProductSet> productSets() {
        return productSets;
    }

    public List<SelectionRule> selectionRules() {
        return selectionRules;
    }

    /**
     * Validates that {@code selection} satisfies all configured {@link SelectionRule}s.
     * Returns a failure result with descriptive error messages when rules are violated.
     */
    public PackageValidationResult validate(List<SelectedProduct> selection) {
        var errors = new ArrayList<String>();

        for (SelectionRule rule : selectionRules) {
            if (!rule.isSatisfiedBy(selection)) {
                errors.add("Rule not satisfied: " + rule);
            }
        }

        return errors.isEmpty()
                ? PackageValidationResult.success()
                : PackageValidationResult.failure(errors);
    }
}
