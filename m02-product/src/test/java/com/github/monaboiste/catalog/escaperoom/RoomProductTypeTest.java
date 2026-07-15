package com.github.monaboiste.catalog.escaperoom;

import com.github.monaboiste.catalog.product.ProductTrackingStrategy;
import com.github.monaboiste.catalog.product.feature.ProductFeatureType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task point 1 — room ProductTypes.
 *
 * <p>Shows how identity attributes (difficulty, capacity) live in ProductMetadata, how
 * ProductFeatureTypes enforce value constraints, and how the tracking strategy is declared.
 */
class RoomProductTypeTest {

    @Test
    void room_exposes_difficulty_and_capacity_as_metadata() {
        // Metadata encodes identity: changing these values would mean a different product.
        assertThat(EscapeRoomCatalog.MAD_SCIENTIST_LAB.metadata().get("difficulty"))
                .isEqualTo(Optional.of("medium"));
        assertThat(EscapeRoomCatalog.MAD_SCIENTIST_LAB.metadata().get("durationMinutes"))
                .isEqualTo(Optional.of("60"));
        assertThat(EscapeRoomCatalog.MAD_SCIENTIST_LAB.metadata().get("minParticipants"))
                .isEqualTo(Optional.of("2"));
        assertThat(EscapeRoomCatalog.MAD_SCIENTIST_LAB.metadata().get("maxParticipants"))
                .isEqualTo(Optional.of("5"));
    }

    @Test
    void cyberpunk_exposes_vr_requirement_in_metadata() {
        assertThat(EscapeRoomCatalog.CYBERPUNK_2077.metadata().get("requiresVr"))
                .isEqualTo(Optional.of("true"));
        assertThat(EscapeRoomCatalog.CYBERPUNK_2077.metadata().get("difficulty"))
                .isEqualTo(Optional.of("extreme"));
    }

    @Test
    void participants_feature_accepts_value_within_room_range() {
        // Mad Scientist's Lab accepts 2–5 participants.
        Optional<ProductFeatureType> feature = EscapeRoomCatalog.MAD_SCIENTIST_LAB
                .featureTypes().getFeatureType("participants");

        assertThat(feature).isPresent();
        assertThat(feature.get().isValid(3)).isTrue();
        assertThat(feature.get().isValid(5)).isTrue();
    }

    @Test
    void participants_feature_rejects_value_outside_room_range() {
        // The room's upper capacity is 5 — 6 people should fail validation without any
        // controller code needed.
        Optional<ProductFeatureType> feature = EscapeRoomCatalog.MAD_SCIENTIST_LAB
                .featureTypes().getFeatureType("participants");

        assertThat(feature).isPresent();
        assertThat(feature.get().isValid(1)).isFalse(); // below minimum
        assertThat(feature.get().isValid(6)).isFalse(); // above maximum
    }

    @Test
    void room_uses_individually_tracked_strategy() {
        // Every booking is a distinct instance — the strategy drives the requirement
        // for a SerialNumber (booking reference) on every ProductInstance.
        assertThat(EscapeRoomCatalog.MAD_SCIENTIST_LAB.trackingStrategy())
                .isEqualTo(ProductTrackingStrategy.INDIVIDUALLY_TRACKED);
        assertThat(EscapeRoomCatalog.ALCATRAZ.trackingStrategy())
                .isEqualTo(ProductTrackingStrategy.INDIVIDUALLY_TRACKED);
        assertThat(EscapeRoomCatalog.EGYPTIAN_TOMB.trackingStrategy())
                .isEqualTo(ProductTrackingStrategy.INDIVIDUALLY_TRACKED);
        assertThat(EscapeRoomCatalog.CYBERPUNK_2077.trackingStrategy())
                .isEqualTo(ProductTrackingStrategy.INDIVIDUALLY_TRACKED);
    }

    @Test
    void participants_feature_is_mandatory_for_all_rooms() {
        // Mandatory features must be set on every booking — optional ones may be omitted.
        assertThat(EscapeRoomCatalog.MAD_SCIENTIST_LAB.featureTypes().isMandatory("participants"))
                .isTrue();
        assertThat(EscapeRoomCatalog.ALCATRAZ.featureTypes().isMandatory("participants"))
                .isTrue();
        assertThat(EscapeRoomCatalog.CYBERPUNK_2077.featureTypes().isMandatory("participants"))
                .isTrue();
    }
}
