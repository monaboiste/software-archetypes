package com.github.monaboiste.catalog.escaperoom;

import com.github.monaboiste.catalog.applicability.ApplicabilityContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task point 4 — ApplicabilityConstraints for Cyberpunk 2077 and Hardcore.
 *
 * <p>Shows that the product itself answers "am I applicable here?" — no controller logic, no SQL
 * filter, no frontend guard needed. The context is a flat map that travels across service
 * boundaries and can be built from HTTP headers, JWT claims, or a booking form.
 */
class ApplicabilityConstraintTest {

    @Test
    void cyberpunk_available_only_in_warsaw_with_vr() {
        ApplicabilityContext warsawWithVr = ApplicabilityContext.of(Map.of(
                "city",           "Warsaw",
                "hasVrEquipment", "true",
                "age",            "25"
        ));

        assertThat(EscapeRoomCatalog.CYBERPUNK_2077.isApplicableFor(warsawWithVr)).isTrue();
    }

    @Test
    void cyberpunk_not_available_outside_warsaw() {
        // VR equipment is only installed in Warsaw — other cities cannot offer this room.
        ApplicabilityContext cracowWithVr = ApplicabilityContext.of(Map.of(
                "city",           "Cracow",
                "hasVrEquipment", "true"
        ));

        assertThat(EscapeRoomCatalog.CYBERPUNK_2077.isApplicableFor(cracowWithVr)).isFalse();
    }

    @Test
    void cyberpunk_not_available_in_warsaw_without_vr_equipment() {
        // Both conditions must be true — city AND VR availability.
        ApplicabilityContext warsawNoVr = ApplicabilityContext.of(Map.of(
                "city",           "Warsaw",
                "hasVrEquipment", "false"
        ));

        assertThat(EscapeRoomCatalog.CYBERPUNK_2077.isApplicableFor(warsawNoVr)).isFalse();
    }

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
        // This is a legal/safety constraint, not a product variant.
        ApplicabilityContext claustrophobic = ApplicabilityContext.of(Map.of(
                "claustrophobia", "true"
        ));

        assertThat(EscapeRoomCatalog.ALCATRAZ.isApplicableFor(claustrophobic)).isFalse();
    }

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

    @Test
    void actor_available_on_weekends() {
        ApplicabilityContext saturday = ApplicabilityContext.of(Map.of("dayType", "Saturday"));
        ApplicabilityContext sunday   = ApplicabilityContext.of(Map.of("dayType", "Sunday"));

        assertThat(EscapeRoomCatalog.ACTOR.isApplicableFor(saturday)).isTrue();
        assertThat(EscapeRoomCatalog.ACTOR.isApplicableFor(sunday)).isTrue();
    }

    @Test
    void actor_not_available_on_weekdays() {
        ApplicabilityContext monday = ApplicabilityContext.of(Map.of("dayType", "Monday"));

        assertThat(EscapeRoomCatalog.ACTOR.isApplicableFor(monday)).isFalse();
    }
}
