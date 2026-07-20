package com.github.monaboiste.catalog.escaperoom;

import com.github.monaboiste.catalog.applicability.ApplicabilityConstraint;
import com.github.monaboiste.catalog.bundle.PackageType;
import com.github.monaboiste.catalog.catalog.CatalogEntry;
import com.github.monaboiste.catalog.product.ProductDescription;
import com.github.monaboiste.catalog.product.ProductIdentifier;
import com.github.monaboiste.catalog.product.ProductName;
import com.github.monaboiste.catalog.product.ProductTrackingStrategy;
import com.github.monaboiste.catalog.product.ProductType;
import com.github.monaboiste.catalog.product.Unit;
import com.github.monaboiste.catalog.product.feature.ProductFeatureType;
import com.github.monaboiste.catalog.relationship.NoSelfRelationshipPolicy;
import com.github.monaboiste.catalog.relationship.ProductRelationship;
import com.github.monaboiste.catalog.relationship.ProductRelationshipFactory;
import com.github.monaboiste.catalog.relationship.ProductRelationshipType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.monaboiste.catalog.applicability.ApplicabilityConstraint.and;
import static com.github.monaboiste.catalog.applicability.ApplicabilityConstraint.eq;
import static com.github.monaboiste.catalog.applicability.ApplicabilityConstraint.greaterThan;
import static com.github.monaboiste.catalog.applicability.ApplicabilityConstraint.in;
import static com.github.monaboiste.catalog.applicability.ApplicabilityConstraint.not;

/**
 * Catalogue of escape-room products for a three-city chain.
 *
 * <p>Answers the five task points:
 * <ol>
 *   <li>Room {@link ProductType}s with features and metadata (point 1)
 *   <li>Add-on {@link ProductType}s; catering as one type with a variant feature (point 2)
 *   <li>Team Building {@link PackageType} with selection rules (point 3)
 *   <li>{@link ApplicabilityConstraint}s for Cyberpunk and Hardcore (point 4)
 *   <li>Relationships between rooms and add-ons (point 5)
 * </ol>
 *
 * <p>All identifiers are fixed string constants so that relationships, packages, and tests can
 * compose them without coupling to specific object references.
 */
public final class EscapeRoomCatalog {

    private EscapeRoomCatalog() {
    }

    // =========================================================================
    // Fixed product identifiers — stable across the catalogue
    // =========================================================================

    // Rooms
    public static final ProductIdentifier ID_MAD_SCIENTIST_LAB = ProductIdentifier.of("ROOM_MAD_SCIENTIST_LAB");
    public static final ProductIdentifier ID_ALCATRAZ = ProductIdentifier.of("ROOM_ALCATRAZ");
    public static final ProductIdentifier ID_EGYPTIAN_TOMB = ProductIdentifier.of("ROOM_EGYPTIAN_TOMB");
    public static final ProductIdentifier ID_CYBERPUNK_2077 = ProductIdentifier.of("ROOM_CYBERPUNK_2077");

    // Add-ons
    public static final ProductIdentifier ID_ACTOR = ProductIdentifier.of("ADDON_ACTOR");
    public static final ProductIdentifier ID_PHOTO_VIDEO = ProductIdentifier.of("ADDON_PHOTO_VIDEO");
    public static final ProductIdentifier ID_CATERING = ProductIdentifier.of("ADDON_CATERING");
    public static final ProductIdentifier ID_DEDICATED_GM = ProductIdentifier.of("ADDON_DEDICATED_GM");

    // Packages
    public static final ProductIdentifier ID_TEAM_BUILDING = ProductIdentifier.of("PKG_TEAM_BUILDING");
    public static final ProductIdentifier ID_HARDCORE = ProductIdentifier.of("PKG_HARDCORE");

    // =========================================================================
    // Rooms  (point 1)
    //
    // Design decisions:
    //   METADATA  — attributes that define what the room IS: difficulty, duration, capacity,
    //               VR requirement. Changing any of them would mean a different product.
    //   FEATURES  — attributes the booking flow configures: how many participants are coming.
    //               The range is capped to the room's actual min/max capacity stored in metadata.
    //   TRACKING  — INDIVIDUALLY_TRACKED: every booking is a unique instance with its own
    //               serial number (booking reference), enabling per-booking audit and fulfilment.
    // =========================================================================

    /**
     * 60 min · medium difficulty · 2–5 persons.
     */
    public static final ProductType MAD_SCIENTIST_LAB = ProductType
            .builder(
                    ID_MAD_SCIENTIST_LAB,
                    ProductName.of("Mad Scientist's Laboratory"),
                    ProductDescription.of("Dismantle the professor's doomsday device before time runs out."),
                    Unit.of("booking"),
                    ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .withMetadata(Map.of(
                    "difficulty", "medium",
                    "durationMinutes", "60",
                    "minParticipants", "2",
                    "maxParticipants", "5"))
            .withMandatoryFeature(ProductFeatureType.withNumericRange("participants", 2, 5))
            .build();

    /**
     * 75 min · hard difficulty · 3–6 persons. Not available for guests with claustrophobia.
     */
    public static final ProductType ALCATRAZ = ProductType
            .builder(
                    ID_ALCATRAZ,
                    ProductName.of("Alcatraz Prison"),
                    ProductDescription.of("Escape from the most notorious prison in history."),
                    Unit.of("booking"),
                    ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .withMetadata(Map.of(
                    "difficulty", "hard",
                    "durationMinutes", "75",
                    "minParticipants", "3",
                    "maxParticipants", "6"))
            .withMandatoryFeature(ProductFeatureType.withNumericRange("participants", 3, 6))
            // Applicability: guests must declare they do not suffer from claustrophobia.
            // The declaration travels in ApplicabilityContext so the booking flow
            // can enforce it without any product-type code change.
            .withApplicabilityConstraint(not(eq("claustrophobia", "true")))
            .build();

    /**
     * 45 min · easy difficulty · 2–4 persons.
     */
    public static final ProductType EGYPTIAN_TOMB = ProductType
            .builder(
                    ID_EGYPTIAN_TOMB,
                    ProductName.of("Egyptian Tomb"),
                    ProductDescription.of("Survive the ancient curse and find your way out."),
                    Unit.of("booking"),
                    ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .withMetadata(Map.of(
                    "difficulty", "easy",
                    "durationMinutes", "45",
                    "minParticipants", "2",
                    "maxParticipants", "4"))
            .withMandatoryFeature(ProductFeatureType.withNumericRange("participants", 2, 4))
            .build();

    /**
     * 90 min · extreme difficulty · 4–6 persons · requires VR.
     *
     * <p>Warsaw-only availability is expressed by this room appearing <em>only</em> in the Warsaw
     * {@link CatalogEntry} (see {@link #catalogEntriesFor}), with the VR-equipment check carried
     * as a {@code salesConstraint} on that entry. Following L08: the product definition captures
     * what the room IS (VR required → metadata); the catalog entry captures where/when it is
     * offered (Warsaw, operational VR available).
     */
    public static final ProductType CYBERPUNK_2077 = ProductType
            .builder(
                    ID_CYBERPUNK_2077,
                    ProductName.of("Cyberpunk 2077"),
                    ProductDescription.of("Hack the megacorp's mainframe in full virtual reality."),
                    Unit.of("booking"),
                    ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .withMetadata(Map.of(
                    "difficulty", "extreme",
                    "durationMinutes", "90",
                    "minParticipants", "4",
                    "maxParticipants", "6",
                    "requiresVr", "true"))   // intrinsic: what this room IS
            .withMandatoryFeature(ProductFeatureType.withNumericRange("participants", 4, 6))
            .build();

    // =========================================================================
    // Add-ons  (point 2)
    //
    // Design decision — catering as ONE ProductType with a "variant" feature:
    //   All three catering options (pizza / sushi / vegetarian) share the same:
    //     • tracking strategy (INDIVIDUALLY_TRACKED, one catering order per booking)
    //     • pricing logic (same base price bracket, +30% per booking)
    //     • applicability (no special constraints)
    //     • operational lifecycle (ordered, prepared, delivered after the game)
    //   The only difference is which meal is prepared — a configurable choice, not a
    //   structural difference. Modelling them as one ProductType with an AllowedValues
    //   feature avoids duplicating three near-identical definitions and keeps the catalogue
    //   flat. If pricing or availability ever diverges per variant, promote to separate types.
    // =========================================================================

    /**
     * Catering served after the game. Variant (meal choice) is a mandatory feature.
     * One ProductType — three meals are variants, not separate products.
     */
    public static final ProductType CATERING = ProductType
            .builder(
                    ID_CATERING,
                    ProductName.of("Post-game Catering"),
                    ProductDescription.of("A meal served after the game. Choose your menu."),
                    Unit.of("order"),
                    ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .withMandatoryFeature(
                    ProductFeatureType.withAllowedValues("variant", "pizza", "sushi", "vegetarian"))
            .build();

    /**
     * A live actor inside the room (+30% to base price). Available on weekends only.
     *
     * <p>Weekend-only availability is a scheduling/operational constraint — it depends on
     * when you book, not on what the actor add-on IS. It lives on the {@link CatalogEntry}
     * as a {@code salesConstraint} (see {@link #catalogEntriesFor}), not on this product type.
     * A future corporate-events catalog could offer actors on weekdays without changing this type.
     *
     * <p>Compare with Alcatraz's claustrophobia check: that stays on the {@code ProductType}
     * because it is intrinsic to the room, not to when/where it is sold.
     */
    public static final ProductType ACTOR = ProductType
            .builder(
                    ID_ACTOR,
                    ProductName.of("Actor in the Room"),
                    ProductDescription.of("A professional actor joins your session for extra immersion."),
                    Unit.of("session"),
                    ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .build();

    /**
     * Photo and video package — full recording of the game session.
     */
    public static final ProductType PHOTO_VIDEO = ProductType
            .builder(
                    ID_PHOTO_VIDEO,
                    ProductName.of("Photo & Video Package"),
                    ProductDescription.of("High-quality photos and a highlight video of your escape."),
                    Unit.of("session"),
                    ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .build();

    /**
     * Dedicated game master — a specialist assigned instead of the standard GM.
     */
    public static final ProductType DEDICATED_GM = ProductType
            .builder(
                    ID_DEDICATED_GM,
                    ProductName.of("Dedicated Game Master"),
                    ProductDescription.of("A senior GM exclusively assigned to your group."),
                    Unit.of("session"),
                    ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .build();

    // =========================================================================
    // Packages  (points 3 & 4)
    // =========================================================================

    /**
     * Team Building package — two rooms played sequentially, catering after, dedicated GM.
     *
     * <p>Selection rules: exactly two rooms from the full catalogue; catering mandatory (≥1);
     * dedicated GM mandatory (exactly 1). The sequential ordering is an operational concern
     * handled by fulfilment — the product catalogue defines <em>what</em>, not <em>when</em>.
     */
    public static final PackageType TEAM_BUILDING = buildTeamBuilding();

    /**
     * Hardcore package — Cyberpunk 2077 + actor + dedicated GM.
     * For adults only (18+). The age check uses {@link ApplicabilityConstraint#greaterThan}
     * with threshold 17 (strictly greater than 17 == at least 18).
     */
    public static final PackageType HARDCORE = buildHardcore();

    // =========================================================================
    // Private builders (called once during static init — avoids forward-reference issues)
    // =========================================================================

    private static PackageType buildTeamBuilding() {
        return PackageType
                .builder(
                        ID_TEAM_BUILDING,
                        ProductName.of("Team Building"),
                        ProductDescription.of(
                                "Two escape rooms played back-to-back with catering and a dedicated GM."))
                // Exactly two different rooms must be chosen.
                .withChoice("Rooms", 2, 2, ID_MAD_SCIENTIST_LAB, ID_ALCATRAZ, ID_EGYPTIAN_TOMB, ID_CYBERPUNK_2077)
                // Catering is non-negotiable — the event includes a meal.
                .withRequiredChoice("Catering", ID_CATERING)
                // A dedicated GM coordinates both sessions.
                .withRequiredChoice("DedicatedGameMaster", ID_DEDICATED_GM)
                .build();
    }

    private static PackageType buildHardcore() {
        return PackageType
                .builder(
                        ID_HARDCORE,
                        ProductName.of("Hardcore"),
                        ProductDescription.of(
                                "Cyberpunk 2077 with a live actor and a dedicated GM. Adults only (18+)."))
                .withSingleChoice("HardcoreRoom", ID_CYBERPUNK_2077)
                .withSingleChoice("Actor", ID_ACTOR)
                .withSingleChoice("DedicatedGameMaster", ID_DEDICATED_GM)
                // Applicability: 18+ only. Passed as "age" in ApplicabilityContext.
                .withApplicabilityConstraint(greaterThan("age", 17))
                .build();
    }

    // =========================================================================
    // Relationships  (point 5)
    //
    // Rooms form a difficulty progression: easy → medium → hard → extreme.
    // This models the "you did the easy one, want to try the next level?" upsell path.
    // Rooms are complemented by all add-ons — the catalogue engine can use this to
    // suggest relevant extras during booking.
    //
    // Mutually exclusive rooms: Cyberpunk 2077 is location-locked to Warsaw and needs a
    // 90-min VR slot. It cannot be sequentially paired with the other rooms in a single
    // Team-Building session held outside Warsaw. INCOMPATIBLE_WITH edges express this as a
    // soft catalogue hint — eventual consistency, per L07 (a 5-minute propagation window
    // where an incompatible pairing is temporarily possible is acceptable; caught at booking).
    // =========================================================================

    /**
     * Returns all product relationships for the escape-room catalogue.
     *
     * <p>The {@link NoSelfRelationshipPolicy} is the only policy needed here — it prevents
     * obvious data errors. A real implementation would also add NoCyclicUpgradePolicy backed
     * by a repository query.
     */
    public static List<ProductRelationship> relationships() {
        var factory = new ProductRelationshipFactory(new NoSelfRelationshipPolicy());

        return List.of(
                // Difficulty upgrade path (directional: easier → harder)
                factory.define(ID_EGYPTIAN_TOMB, ID_MAD_SCIENTIST_LAB, ProductRelationshipType.UPGRADABLE_TO),
                factory.define(ID_MAD_SCIENTIST_LAB, ID_ALCATRAZ, ProductRelationshipType.UPGRADABLE_TO),
                factory.define(ID_ALCATRAZ, ID_CYBERPUNK_2077, ProductRelationshipType.UPGRADABLE_TO),

                // Rooms are enhanced by add-ons — used by the catalogue to suggest extras
                factory.define(ID_MAD_SCIENTIST_LAB, ID_ACTOR, ProductRelationshipType.COMPLEMENTED_BY),
                factory.define(ID_MAD_SCIENTIST_LAB, ID_CATERING, ProductRelationshipType.COMPLEMENTED_BY),
                factory.define(ID_MAD_SCIENTIST_LAB, ID_PHOTO_VIDEO, ProductRelationshipType.COMPLEMENTED_BY),
                factory.define(ID_ALCATRAZ, ID_ACTOR, ProductRelationshipType.COMPLEMENTED_BY),
                factory.define(ID_ALCATRAZ, ID_CATERING, ProductRelationshipType.COMPLEMENTED_BY),
                factory.define(ID_ALCATRAZ, ID_PHOTO_VIDEO, ProductRelationshipType.COMPLEMENTED_BY),
                factory.define(ID_EGYPTIAN_TOMB, ID_ACTOR, ProductRelationshipType.COMPLEMENTED_BY),
                factory.define(ID_EGYPTIAN_TOMB, ID_CATERING, ProductRelationshipType.COMPLEMENTED_BY),
                factory.define(ID_EGYPTIAN_TOMB, ID_PHOTO_VIDEO, ProductRelationshipType.COMPLEMENTED_BY),
                factory.define(ID_CYBERPUNK_2077, ID_ACTOR, ProductRelationshipType.COMPLEMENTED_BY),
                factory.define(ID_CYBERPUNK_2077, ID_CATERING, ProductRelationshipType.COMPLEMENTED_BY),
                factory.define(ID_CYBERPUNK_2077, ID_PHOTO_VIDEO, ProductRelationshipType.COMPLEMENTED_BY),

                // Mutually exclusive rooms: Cyberpunk 2077 cannot be combined with other rooms
                // in one Team-Building session — it requires Warsaw + VR, while the others run
                // in all three cities. Directionality: Cyberpunk is the constraint source.
                factory.define(ID_CYBERPUNK_2077, ID_EGYPTIAN_TOMB, ProductRelationshipType.INCOMPATIBLE_WITH),
                factory.define(ID_CYBERPUNK_2077, ID_MAD_SCIENTIST_LAB, ProductRelationshipType.INCOMPATIBLE_WITH),
                factory.define(ID_CYBERPUNK_2077, ID_ALCATRAZ, ProductRelationshipType.INCOMPATIBLE_WITH)
        );
    }

    // =========================================================================
    // Catalog entries — "world of sales" (L06)
    //
    // Same product definitions, different offers per city. Cyberpunk 2077 appears only in
    // the Warsaw catalog (VR equipment installed there). Every other room and add-on is
    // available in all three cities.
    //
    // Per L08: sales-context constraints (city, channel, day-of-week) live on CatalogEntry.
    //   • Actor's weekend-only availability → salesConstraint on the entry, not on the type.
    //   • Cyberpunk's Warsaw + VR check → salesConstraint on the Warsaw entry only.
    //   • Alcatraz's claustrophobia check → stays on the ProductType (intrinsic safety rule).
    //   • Hardcore's 18+ check → stays on the PackageType (intrinsic legal constraint).
    // =========================================================================

    /**
     * Returns catalog entries for the named city.
     *
     * <p>Cyberpunk 2077 is absent from the Kraków and Wrocław catalogs — the VR equipment
     * is only installed in Warsaw. The absence of an entry IS the availability expression;
     * no negative constraint is needed on the other cities' entries.
     *
     * @param city one of {@code "Warsaw"}, {@code "Krakow"}, {@code "Wroclaw"}
     */
    public static List<CatalogEntry> catalogEntriesFor(String city) {
        List<CatalogEntry> entries = new ArrayList<>();

        // --- Rooms (available in all cities) ---
        entries.add(CatalogEntry.always(MAD_SCIENTIST_LAB, MAD_SCIENTIST_LAB.name().toString(), Set.of("room")));
        entries.add(CatalogEntry.always(ALCATRAZ, ALCATRAZ.name().toString(), Set.of("room")));
        entries.add(CatalogEntry.always(EGYPTIAN_TOMB, EGYPTIAN_TOMB.name().toString(), Set.of("room")));

        // Cyberpunk only in Warsaw — VR equipment lives there.
        // The salesConstraint also gates on hasVrEquipment so that operational downtime
        // (VR broken) can be reflected in the booking context without editing the catalog.
        if ("Warsaw".equals(city)) {
            entries.add(CatalogEntry.withSalesConstraint(
                    CYBERPUNK_2077,
                    CYBERPUNK_2077.name().toString(),
                    Set.of("room"),
                    and(eq("city", "Warsaw"), eq("hasVrEquipment", "true"))));
        }

        // --- Add-ons ---
        // Actor: available everywhere, but only on weekends (sales/scheduling constraint).
        entries.add(CatalogEntry.withSalesConstraint(
                ACTOR,
                ACTOR.name().toString(),
                Set.of("addon"),
                in("dayType", "Saturday", "Sunday")));
        entries.add(CatalogEntry.always(PHOTO_VIDEO, PHOTO_VIDEO.name().toString(), Set.of("addon")));
        entries.add(CatalogEntry.always(CATERING, CATERING.name().toString(), Set.of("addon")));
        entries.add(CatalogEntry.always(DEDICATED_GM, DEDICATED_GM.name().toString(), Set.of("addon")));

        // --- Packages ---
        entries.add(CatalogEntry.always(TEAM_BUILDING, TEAM_BUILDING.name().toString(), Set.of("package")));
        entries.add(CatalogEntry.always(HARDCORE, HARDCORE.name().toString(), Set.of("package")));

        return List.copyOf(entries);
    }
}
