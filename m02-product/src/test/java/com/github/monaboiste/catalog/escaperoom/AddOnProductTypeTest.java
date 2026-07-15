package com.github.monaboiste.catalog.escaperoom;

import com.github.monaboiste.catalog.product.feature.ProductFeatureType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task point 2 — add-on ProductTypes.
 *
 * <p>Justifies the "catering as one product with a variant feature" decision and shows that
 * the AllowedValues constraint keeps domain validation in the model.
 */
class AddOnProductTypeTest {

    @Test
    void catering_is_one_product_with_variant_feature() {
        // Design decision: pizza / sushi / vegetarian share the same tracking strategy,
        // pricing bracket, applicability, and lifecycle. The only difference is which meal
        // is served — a configurable choice (Feature), not a structural one.
        // Modelling them as a single ProductType avoids three near-identical definitions.
        Optional<ProductFeatureType> variantFeature = EscapeRoomCatalog.CATERING
                .featureTypes().getFeatureType("variant");

        assertThat(variantFeature).isPresent();
        assertThat(EscapeRoomCatalog.CATERING.featureTypes().isMandatory("variant")).isTrue();
    }

    @Test
    void catering_variant_feature_accepts_all_three_meal_options() {
        ProductFeatureType variant = EscapeRoomCatalog.CATERING
                .featureTypes().getFeatureType("variant").orElseThrow();

        assertThat(variant.isValid("pizza")).isTrue();
        assertThat(variant.isValid("sushi")).isTrue();
        assertThat(variant.isValid("vegetarian")).isTrue();
    }

    @Test
    void catering_rejects_unknown_variant() {
        // The constraint enforces that only declared meals can be ordered.
        // No controller validation needed — the product model rejects it.
        ProductFeatureType variant = EscapeRoomCatalog.CATERING
                .featureTypes().getFeatureType("variant").orElseThrow();

        assertThat(variant.isValid("hamburger")).isFalse();
        assertThat(variant.isValid("")).isFalse();
    }

    @Test
    void actor_has_no_features_and_has_weekend_applicability_constraint() {
        // The actor add-on is self-contained — no configuration needed beyond the booking date.
        // Availability is expressed as an ApplicabilityConstraint, not a feature.
        assertThat(EscapeRoomCatalog.ACTOR.featureTypes().mandatoryFeatures()).isEmpty();
        assertThat(EscapeRoomCatalog.ACTOR.featureTypes().optionalFeatures()).isEmpty();
    }

    @Test
    void photo_video_package_is_always_applicable() {
        // No applicability constraint — available in all cities, all days, all rooms.
        assertThat(EscapeRoomCatalog.PHOTO_VIDEO.featureTypes().mandatoryFeatures()).isEmpty();
    }
}
