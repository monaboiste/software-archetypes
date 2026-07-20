package com.github.monaboiste.catalog.escaperoom;

import com.github.monaboiste.catalog.product.ProductInstance;
import com.github.monaboiste.catalog.product.SerialNumber;
import com.github.monaboiste.catalog.product.feature.ProductFeatureInstance;
import com.github.monaboiste.catalog.product.feature.ProductFeatureType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Task points 1 & 2 — feature instances close the feature loop (L05).
 *
 * <p>Features are defined on the type (what CAN be configured); instances record what WAS chosen.
 * Validation against {@link com.github.monaboiste.catalog.product.feature.FeatureValueConstraint}
 * happens at construction — the invariant lives in the model, not in controllers.
 */
class ProductInstanceFeatureTest {

    // -------------------------------------------------------------------------
    // Catering — AllowedValues constraint (variant: pizza / sushi / vegetarian)
    // -------------------------------------------------------------------------

    @Test
    void catering_booking_with_valid_variant_constructs() {
        ProductFeatureType variantType =
                ProductFeatureType.withAllowedValues("variant", "pizza", "sushi", "vegetarian");

        ProductInstance booking = ProductInstance.of(
                EscapeRoomCatalog.CATERING,
                SerialNumber.of("CAT-2025-001"),
                List.of(ProductFeatureInstance.of(variantType, "sushi")));

        assertThat(booking.feature("variant")).isNotNull();
        assertThat(booking.feature("variant").value()).isEqualTo("sushi");
    }

    @Test
    void catering_booking_with_invalid_variant_is_rejected() {
        // "burger" is not in the allowed set — the ProductFeatureInstance ctor throws.
        ProductFeatureType variantType =
                ProductFeatureType.withAllowedValues("variant", "pizza", "sushi", "vegetarian");

        assertThatThrownBy(() ->
                ProductInstance.of(
                        EscapeRoomCatalog.CATERING,
                        SerialNumber.of("CAT-2025-002"),
                        List.of(ProductFeatureInstance.of(variantType, "burger"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void catering_booking_without_variant_is_rejected() {
        // "variant" is mandatory — missing it fails at ProductInstance construction.
        assertThatThrownBy(() ->
                ProductInstance.of(
                        EscapeRoomCatalog.CATERING,
                        SerialNumber.of("CAT-2025-003"),
                        List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("variant");
    }

    // -------------------------------------------------------------------------
    // Room — NumericRange constraint (participants in room capacity)
    // -------------------------------------------------------------------------

    @Test
    void room_booking_with_valid_participant_count_constructs() {
        ProductFeatureType participantsType =
                ProductFeatureType.withNumericRange("participants", 2, 5);

        ProductInstance booking = ProductInstance.of(
                EscapeRoomCatalog.MAD_SCIENTIST_LAB,
                SerialNumber.of("BOOKING-2025-LAB-01"),
                List.of(ProductFeatureInstance.of(participantsType, 4)));

        assertThat(booking.feature("participants")).isNotNull();
        assertThat(booking.feature("participants").value()).isEqualTo(4);
    }

    @Test
    void room_booking_with_too_many_participants_is_rejected() {
        // 6 people exceed Mad Scientist's max capacity of 5.
        ProductFeatureType participantsType =
                ProductFeatureType.withNumericRange("participants", 2, 5);

        assertThatThrownBy(() ->
                ProductInstance.of(
                        EscapeRoomCatalog.MAD_SCIENTIST_LAB,
                        SerialNumber.of("BOOKING-2025-LAB-02"),
                        List.of(ProductFeatureInstance.of(participantsType, 6))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void room_booking_without_participants_is_rejected() {
        // "participants" is a mandatory feature — missing it at instance creation is an error.
        assertThatThrownBy(() ->
                ProductInstance.of(
                        EscapeRoomCatalog.MAD_SCIENTIST_LAB,
                        SerialNumber.of("BOOKING-2025-LAB-03"),
                        List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("participants");
    }
}
