package com.github.monaboiste.catalog.escaperoom;

import com.github.monaboiste.catalog.bundle.PackageInstance;
import com.github.monaboiste.catalog.bundle.SelectedInstance;
import com.github.monaboiste.catalog.product.ProductInstance;
import com.github.monaboiste.catalog.product.SerialNumber;
import com.github.monaboiste.catalog.product.feature.ProductFeatureInstance;
import com.github.monaboiste.catalog.product.feature.ProductFeatureType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Task point 3 — instance-side Composite (L09).
 *
 * <p>Closes the Composite symmetrically: just as {@code ProductType}/{@code PackageType} are both
 * {@link com.github.monaboiste.catalog.product.Product}s, a room booking and a "Team Building"
 * booking are both {@link com.github.monaboiste.catalog.product.Instance}s. This test exercises
 * the full stack end-to-end: feature validation on sub-instances + tracking + package rules.
 */
class PackageInstanceTest {

    // Feature types for building test instances
    private static final ProductFeatureType PARTICIPANTS_LAB =
            ProductFeatureType.withNumericRange("participants", 2, 5);
    private static final ProductFeatureType PARTICIPANTS_ALCATRAZ =
            ProductFeatureType.withNumericRange("participants", 3, 6);
    private static final ProductFeatureType CATERING_VARIANT =
            ProductFeatureType.withAllowedValues("variant", "pizza", "sushi", "vegetarian");

    // Pre-built sub-instances reused across test cases
    private static final ProductInstance labInstance = ProductInstance.of(
            EscapeRoomCatalog.MAD_SCIENTIST_LAB,
            SerialNumber.of("TB-LAB-001"),
            List.of(ProductFeatureInstance.of(PARTICIPANTS_LAB, 4)));

    private static final ProductInstance alcatrazInstance = ProductInstance.of(
            EscapeRoomCatalog.ALCATRAZ,
            SerialNumber.of("TB-ALC-001"),
            List.of(ProductFeatureInstance.of(PARTICIPANTS_ALCATRAZ, 4)));

    private static final ProductInstance cateringInstance = ProductInstance.of(
            EscapeRoomCatalog.CATERING,
            SerialNumber.of("TB-CAT-001"),
            List.of(ProductFeatureInstance.of(CATERING_VARIANT, "sushi")));

    private static final ProductInstance gmInstance = ProductInstance.of(
            EscapeRoomCatalog.DEDICATED_GM,
            SerialNumber.of("TB-GM-001"));

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void team_building_booking_constructs_with_valid_selection() {
        // Two rooms + catering + GM — satisfies all three selection rules.
        PackageInstance booking = PackageInstance.of(
                EscapeRoomCatalog.TEAM_BUILDING,
                SerialNumber.of("TB-2025-001"),
                List.of(
                        SelectedInstance.of(labInstance),
                        SelectedInstance.of(alcatrazInstance),
                        SelectedInstance.of(cateringInstance),
                        SelectedInstance.of(gmInstance)));

        assertThat(booking.product().id()).isEqualTo(EscapeRoomCatalog.ID_TEAM_BUILDING);
        assertThat(booking.serialNumber()).isEqualTo(SerialNumber.of("TB-2025-001"));
        assertThat(booking.selection()).hasSize(4);
        assertThat(booking.id()).isNotNull();
    }

    @Test
    void package_instance_is_an_instance() {
        // PackageInstance implements Instance — the system can treat it uniformly with
        // ProductInstance (billing, fulfilment, audit) without knowing which kind it is.
        PackageInstance booking = PackageInstance.of(
                EscapeRoomCatalog.TEAM_BUILDING,
                SerialNumber.of("TB-2025-002"),
                List.of(
                        SelectedInstance.of(labInstance),
                        SelectedInstance.of(alcatrazInstance),
                        SelectedInstance.of(cateringInstance),
                        SelectedInstance.of(gmInstance)));

        com.github.monaboiste.catalog.product.Instance instance = booking;
        assertThat(instance.product().id()).isEqualTo(EscapeRoomCatalog.ID_TEAM_BUILDING);
    }

    // -------------------------------------------------------------------------
    // Invalid selections — rejected at construction
    // -------------------------------------------------------------------------

    @Test
    void booking_with_only_one_room_is_rejected() {
        // Team Building requires exactly 2 rooms — 1 violates the rule.
        assertThatThrownBy(() ->
                PackageInstance.of(
                        EscapeRoomCatalog.TEAM_BUILDING,
                        SerialNumber.of("TB-BAD-001"),
                        List.of(
                                SelectedInstance.of(labInstance),
                                SelectedInstance.of(cateringInstance),
                                SelectedInstance.of(gmInstance))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void booking_without_catering_is_rejected() {
        assertThatThrownBy(() ->
                PackageInstance.of(
                        EscapeRoomCatalog.TEAM_BUILDING,
                        SerialNumber.of("TB-BAD-002"),
                        List.of(
                                SelectedInstance.of(labInstance),
                                SelectedInstance.of(alcatrazInstance),
                                SelectedInstance.of(gmInstance))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void booking_without_serial_number_is_rejected() {
        // Package bookings always require a booking reference — unlike some single products
        // where optional tracking is permissible.
        assertThatThrownBy(() ->
                PackageInstance.of(
                        EscapeRoomCatalog.TEAM_BUILDING,
                        null,
                        List.of(
                                SelectedInstance.of(labInstance),
                                SelectedInstance.of(alcatrazInstance),
                                SelectedInstance.of(cateringInstance),
                                SelectedInstance.of(gmInstance))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SerialNumber");
    }
}
