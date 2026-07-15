package com.github.monaboiste.catalog.escaperoom;

import com.github.monaboiste.catalog.bundle.PackageValidationResult;
import com.github.monaboiste.catalog.bundle.SelectedProduct;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task point 3 — Team Building PackageType with selection rules.
 *
 * <p>Shows that a package is a composition of rules, not a list of fields.
 * Valid and invalid selections are verified at the type level via SelectedProduct.
 */
class TeamBuildingPackageTest {

    @Test
    void team_building_requires_exactly_two_rooms_catering_and_gm() {
        // A valid Team Building: two different rooms + catering + dedicated GM.
        List<SelectedProduct> validSelection = List.of(
                SelectedProduct.of(EscapeRoomCatalog.ID_MAD_SCIENTIST_LAB),
                SelectedProduct.of(EscapeRoomCatalog.ID_ALCATRAZ),
                SelectedProduct.of(EscapeRoomCatalog.ID_CATERING),
                SelectedProduct.of(EscapeRoomCatalog.ID_DEDICATED_GM)
        );

        PackageValidationResult result = EscapeRoomCatalog.TEAM_BUILDING
                .validateSelection(validSelection);

        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void team_building_rejects_selection_with_single_room() {
        // The package requires exactly two rooms — one is not enough.
        List<SelectedProduct> tooFewRooms = List.of(
                SelectedProduct.of(EscapeRoomCatalog.ID_ALCATRAZ),
                SelectedProduct.of(EscapeRoomCatalog.ID_CATERING),
                SelectedProduct.of(EscapeRoomCatalog.ID_DEDICATED_GM)
        );

        PackageValidationResult result = EscapeRoomCatalog.TEAM_BUILDING
                .validateSelection(tooFewRooms);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }

    @Test
    void team_building_rejects_selection_missing_catering() {
        List<SelectedProduct> missingCatering = List.of(
                SelectedProduct.of(EscapeRoomCatalog.ID_MAD_SCIENTIST_LAB),
                SelectedProduct.of(EscapeRoomCatalog.ID_ALCATRAZ),
                SelectedProduct.of(EscapeRoomCatalog.ID_DEDICATED_GM)
        );

        PackageValidationResult result = EscapeRoomCatalog.TEAM_BUILDING
                .validateSelection(missingCatering);

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void team_building_rejects_selection_missing_dedicated_gm() {
        List<SelectedProduct> missingGm = List.of(
                SelectedProduct.of(EscapeRoomCatalog.ID_EGYPTIAN_TOMB),
                SelectedProduct.of(EscapeRoomCatalog.ID_CYBERPUNK_2077),
                SelectedProduct.of(EscapeRoomCatalog.ID_CATERING)
        );

        PackageValidationResult result = EscapeRoomCatalog.TEAM_BUILDING
                .validateSelection(missingGm);

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void team_building_rejects_selection_with_three_rooms() {
        // Three rooms violates the "exactly two" rule.
        List<SelectedProduct> tooManyRooms = List.of(
                SelectedProduct.of(EscapeRoomCatalog.ID_EGYPTIAN_TOMB),
                SelectedProduct.of(EscapeRoomCatalog.ID_MAD_SCIENTIST_LAB),
                SelectedProduct.of(EscapeRoomCatalog.ID_ALCATRAZ),
                SelectedProduct.of(EscapeRoomCatalog.ID_CATERING),
                SelectedProduct.of(EscapeRoomCatalog.ID_DEDICATED_GM)
        );

        PackageValidationResult result = EscapeRoomCatalog.TEAM_BUILDING
                .validateSelection(tooManyRooms);

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void hardcore_package_validates_correct_selection() {
        // Hardcore: Cyberpunk 2077 + actor + dedicated GM — all three required.
        List<SelectedProduct> validHardcore = List.of(
                SelectedProduct.of(EscapeRoomCatalog.ID_CYBERPUNK_2077),
                SelectedProduct.of(EscapeRoomCatalog.ID_ACTOR),
                SelectedProduct.of(EscapeRoomCatalog.ID_DEDICATED_GM)
        );

        PackageValidationResult result = EscapeRoomCatalog.HARDCORE
                .validateSelection(validHardcore);

        assertThat(result.isValid()).isTrue();
    }
}
