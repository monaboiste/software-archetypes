package com.github.monaboiste.catalog.escaperoom;

import com.github.monaboiste.catalog.applicability.ApplicabilityConstraint;
import com.github.monaboiste.catalog.bundle.PackageType;
import com.github.monaboiste.catalog.bundle.ProductSet;
import com.github.monaboiste.catalog.bundle.SelectionRule;
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
     * Applicability: Warsaw only (VR equipment is installed there) and the booking context
     * must confirm VR availability.
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
                    "requiresVr", "true"))
            .withMandatoryFeature(ProductFeatureType.withNumericRange("participants", 4, 6))
            .withApplicabilityConstraint(and(
                    eq("city", "Warsaw"),
                    eq("hasVrEquipment", "true")))
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
     * Availability is modelled as an {@link ApplicabilityConstraint} — the booking flow
     * passes the day type in the context; the product itself rejects weekday requests.
     */
    public static final ProductType ACTOR = ProductType
            .builder(
                    ID_ACTOR,
                    ProductName.of("Actor in the Room"),
                    ProductDescription.of("A professional actor joins your session for extra immersion."),
                    Unit.of("session"),
                    ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
            .withApplicabilityConstraint(in("dayType", "Saturday", "Sunday"))
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
     * <p>ProductSets:
     * <ul>
     *   <li>{@code rooms} — any two rooms from the full catalogue (min 2, max 2)
     *   <li>{@code catering} — mandatory catering order (min 1)
     *   <li>{@code dedicatedGm} — mandatory dedicated game master (exactly 1)
     * </ul>
     *
     * <p>SelectionRules enforce the structural contract of the package. The sequential nature
     * of the two rooms is an operational concern captured in the description and handled by
     * fulfilment — the product catalogue defines what, not when.
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
        ProductSet rooms = new ProductSet("Rooms", Set.of(
                ID_MAD_SCIENTIST_LAB, ID_ALCATRAZ, ID_EGYPTIAN_TOMB, ID_CYBERPUNK_2077));
        ProductSet catering = new ProductSet("Catering", Set.of(ID_CATERING));
        ProductSet dedicatedGm = new ProductSet("DedicatedGameMaster", Set.of(ID_DEDICATED_GM));

        return PackageType
                .builder(
                        ID_TEAM_BUILDING,
                        ProductName.of("Team Building"),
                        ProductDescription.of(
                                "Two escape rooms played back-to-back with catering and a dedicated GM."))
                .withProductSet(rooms)
                .withProductSet(catering)
                .withProductSet(dedicatedGm)
                // Exactly two different rooms must be chosen.
                .withSelectionRule(SelectionRule.isSubsetOf(rooms, 2, 2))
                // Catering is non-negotiable — the event includes a meal.
                .withSelectionRule(SelectionRule.required(catering))
                // A dedicated GM coordinates both sessions.
                .withSelectionRule(SelectionRule.required(dedicatedGm))
                .build();
    }

    private static PackageType buildHardcore() {
        ProductSet rooms = new ProductSet("HardcoreRoom", Set.of(ID_CYBERPUNK_2077));
        ProductSet actors = new ProductSet("Actor", Set.of(ID_ACTOR));
        ProductSet gms = new ProductSet("DedicatedGameMaster", Set.of(ID_DEDICATED_GM));

        return PackageType
                .builder(
                        ID_HARDCORE,
                        ProductName.of("Hardcore"),
                        ProductDescription.of(
                                "Cyberpunk 2077 with a live actor and a dedicated GM. Adults only (18+)."))
                .withProductSet(rooms)
                .withProductSet(actors)
                .withProductSet(gms)
                .withSelectionRule(SelectionRule.single(rooms))
                .withSelectionRule(SelectionRule.single(actors))
                .withSelectionRule(SelectionRule.single(gms))
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
                factory.define(ID_CYBERPUNK_2077, ID_PHOTO_VIDEO, ProductRelationshipType.COMPLEMENTED_BY)
        );
    }
}
