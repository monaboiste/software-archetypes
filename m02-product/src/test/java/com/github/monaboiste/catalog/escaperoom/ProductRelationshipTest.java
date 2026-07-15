package com.github.monaboiste.catalog.escaperoom;

import com.github.monaboiste.catalog.relationship.NoSelfRelationshipPolicy;
import com.github.monaboiste.catalog.relationship.ProductRelationship;
import com.github.monaboiste.catalog.relationship.ProductRelationshipFactory;
import com.github.monaboiste.catalog.relationship.ProductRelationshipType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Task point 5 — ProductRelationships between rooms and add-ons.
 *
 * <p>Shows that relationships are first-class entities independent of ProductType, that they form
 * a meaningful domain graph (difficulty upgrade path), and that policies are enforced through
 * the factory.
 */
class ProductRelationshipTest {

    private final ProductRelationshipFactory factory =
            new ProductRelationshipFactory(new NoSelfRelationshipPolicy());

    @Test
    void rooms_form_a_difficulty_upgrade_path() {
        // Easier rooms are UPGRADABLE_TO harder ones — the catalogue can use this to suggest
        // "you completed the easy room, ready for the next challenge?" upsells.
        List<ProductRelationship> relationships = EscapeRoomCatalog.relationships();

        assertThat(relationships)
                .filteredOn(r -> r.type() == ProductRelationshipType.UPGRADABLE_TO)
                .extracting(ProductRelationship::from)
                .containsExactlyInAnyOrder(
                        EscapeRoomCatalog.ID_EGYPTIAN_TOMB,     // easy → medium
                        EscapeRoomCatalog.ID_MAD_SCIENTIST_LAB, // medium → hard
                        EscapeRoomCatalog.ID_ALCATRAZ            // hard → extreme
                );
    }

    @Test
    void upgrade_path_flows_from_easy_to_extreme() {
        List<ProductRelationship> upgrades = EscapeRoomCatalog.relationships().stream()
                .filter(r -> r.type() == ProductRelationshipType.UPGRADABLE_TO)
                .toList();

        // Egyptian Tomb (easy) → Mad Scientist's Lab (medium)
        assertThat(upgrades).anyMatch(r ->
                r.from().equals(EscapeRoomCatalog.ID_EGYPTIAN_TOMB)
                        && r.to().equals(EscapeRoomCatalog.ID_MAD_SCIENTIST_LAB));

        // Mad Scientist's Lab (medium) → Alcatraz (hard)
        assertThat(upgrades).anyMatch(r ->
                r.from().equals(EscapeRoomCatalog.ID_MAD_SCIENTIST_LAB)
                        && r.to().equals(EscapeRoomCatalog.ID_ALCATRAZ));

        // Alcatraz (hard) → Cyberpunk (extreme)
        assertThat(upgrades).anyMatch(r ->
                r.from().equals(EscapeRoomCatalog.ID_ALCATRAZ)
                        && r.to().equals(EscapeRoomCatalog.ID_CYBERPUNK_2077));
    }

    @Test
    void room_is_complemented_by_addons() {
        // Rooms are enhanced by add-ons — the relationship graph drives "you might also like"
        // suggestions in the booking flow without any special-case code.
        List<ProductRelationship> complements = EscapeRoomCatalog.relationships().stream()
                .filter(r -> r.type() == ProductRelationshipType.COMPLEMENTED_BY)
                .toList();

        // Every room should be linked to at least the actor and catering add-ons.
        assertThat(complements).anyMatch(r ->
                r.from().equals(EscapeRoomCatalog.ID_MAD_SCIENTIST_LAB)
                        && r.to().equals(EscapeRoomCatalog.ID_ACTOR));

        assertThat(complements).anyMatch(r ->
                r.from().equals(EscapeRoomCatalog.ID_CYBERPUNK_2077)
                        && r.to().equals(EscapeRoomCatalog.ID_CATERING));
    }

    @Test
    void factory_rejects_self_relationship() {
        // A room "upgradable to itself" is a data error — the NoSelfRelationshipPolicy
        // catches it at creation time, before it can reach any storage.
        assertThatThrownBy(() ->
                factory.define(
                        EscapeRoomCatalog.ID_ALCATRAZ,
                        EscapeRoomCatalog.ID_ALCATRAZ,
                        ProductRelationshipType.UPGRADABLE_TO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("policy");
    }

    @Test
    void factory_creates_valid_cross_product_relationship() {
        // Happy path — two different products, policy passes.
        ProductRelationship rel = factory.define(
                EscapeRoomCatalog.ID_EGYPTIAN_TOMB,
                EscapeRoomCatalog.ID_PHOTO_VIDEO,
                ProductRelationshipType.COMPLEMENTED_BY);

        assertThat(rel.from()).isEqualTo(EscapeRoomCatalog.ID_EGYPTIAN_TOMB);
        assertThat(rel.to()).isEqualTo(EscapeRoomCatalog.ID_PHOTO_VIDEO);
        assertThat(rel.type()).isEqualTo(ProductRelationshipType.COMPLEMENTED_BY);
        assertThat(rel.id()).isNotNull();
    }

    @Test
    void relationships_are_directed_upgrade_does_not_imply_reverse() {
        // "Alcatraz UPGRADABLE_TO Cyberpunk" does NOT mean "Cyberpunk UPGRADABLE_TO Alcatraz".
        // Directionality is explicit — accidental reversal is an intentional design error.
        List<ProductRelationship> upgrades = EscapeRoomCatalog.relationships().stream()
                .filter(r -> r.type() == ProductRelationshipType.UPGRADABLE_TO)
                .toList();

        assertThat(upgrades).noneMatch(r ->
                r.from().equals(EscapeRoomCatalog.ID_CYBERPUNK_2077)
                        && r.to().equals(EscapeRoomCatalog.ID_ALCATRAZ));
    }
}
