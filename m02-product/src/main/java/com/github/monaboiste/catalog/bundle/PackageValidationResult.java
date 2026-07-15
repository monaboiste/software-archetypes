package com.github.monaboiste.catalog.bundle;

import java.util.List;

/**
 * Outcome of validating a product selection against a {@link PackageType}.
 * Carries the full list of violated rules so the caller can surface meaningful errors.
 */
public record PackageValidationResult(boolean valid, List<String> errors) {

    public PackageValidationResult {
        errors = List.copyOf(errors);
    }

    public static PackageValidationResult success() {
        return new PackageValidationResult(true, List.of());
    }

    public static PackageValidationResult failure(List<String> errors) {
        return new PackageValidationResult(false, errors);
    }

    public boolean isValid() {
        return valid;
    }
}
