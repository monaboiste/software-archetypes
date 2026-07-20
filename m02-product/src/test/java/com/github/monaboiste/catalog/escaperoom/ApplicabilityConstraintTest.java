package com.github.monaboiste.catalog.escaperoom;

import com.github.monaboiste.catalog.applicability.ApplicabilityContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task point 4 — ApplicabilityConstraints placed on product types for intrinsic rules.
 *
 * <p>Shows that the product itself answers "am I applicable here?" for safety and legal
 * constraints — no controller logic, no SQL filter, no frontend guard needed.
 *
 * <p>Following L08: constraints that belong to the product's identity (health declaration,
 * legal age) live here on the {@link com.github.monaboiste.catalog.product.ProductType}.
 * Constraints that belong to the sales context (city, channel, day-of-week) live on the
 * {@link com.github.monaboiste.catalog.catalog.CatalogEntry} — see {@link CatalogEntryTest}.
 */
class ApplicabilityConstraintTest {

    // -------------------------------------------------------------------------
    // Alcatraz — intrinsic safety constraint (stays on ProductType)
    // -------------------------------------------------------------------------

    @Test
    void alcatraz_is_available_for_guests_without_claustrophobia() {
        ApplicabilityContext noClaustrophobia = ApplicabilityContext.of(Map.of(
                "claustrophobia", "false"
        ));

        assertThat(EscapeRoomCatalog.ALCATRAZ.isApplicableFor(noClaustrophobia)).isTrue();
    }

    @Test
    void alcatraz_unavailable_for_claustrophobic_guests() {
        // Guest self-declares claustrophobia — the room rejects the booking applicability.
        // This is a safety constraint intrinsic to the room itself, not a sales-context rule:
        // no catalog would ever offer Alcatraz to claustrophobic guests regardless of city.
        ApplicabilityContext claustrophobic = ApplicabilityContext.of(Map.of(
                "claustrophobia", "true"
        ));

        assertThat(EscapeRoomCatalog.ALCATRAZ.isApplicableFor(claustrophobic)).isFalse();
    }

    // -------------------------------------------------------------------------
    // Hardcore package — intrinsic legal constraint (stays on PackageType)
    // -------------------------------------------------------------------------

    @Test
    void hardcore_available_only_for_adults() {
        ApplicabilityContext adult = ApplicabilityContext.of(Map.of("age", "18"));

        assertThat(EscapeRoomCatalog.HARDCORE.isApplicableFor(adult)).isTrue();
    }

    @Test
    void hardcore_not_available_for_minors() {
        // The age check uses greaterThan(17): strictly greater than 17 == at least 18.
        ApplicabilityContext minor = ApplicabilityContext.of(Map.of("age", "17"));

        assertThat(EscapeRoomCatalog.HARDCORE.isApplicableFor(minor)).isFalse();
    }

    @Test
    void hardcore_not_available_when_age_is_missing_from_context() {
        // Missing parameter → constraint returns false → product is not applicable.
        ApplicabilityContext noAge = ApplicabilityContext.of(Map.of("city", "Warsaw"));

        assertThat(EscapeRoomCatalog.HARDCORE.isApplicableFor(noAge)).isFalse();
    }
}
