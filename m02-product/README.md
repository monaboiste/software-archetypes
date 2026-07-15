# M02 — Product and catalog: escape room network

Modelling the offering of an escape room network across three cities using the **Product archetype** from module
M02. The solution covers all five task points and illustrates each pattern with working tests.

## Running

```bash
sdk env
./gradlew test
```

---

## Package structure

```text
src/main/java/com/github/monaboiste/catalog/
│
├── product/                  # Archetype core: product definition and instance
│   └── feature/              # Feature model: feature types and constraints
│   Product.java              # Composite contract — interface
│   ProductType.java          # Product definition (offering template)
│   ProductInstance.java      # Concrete booking / fulfilment
│   ProductMetadata.java      # Identity attributes (immutable)
│   ProductTrackingStrategy.java
│
├── applicability/               # Product applicability rules
│   ApplicabilityContext.java    # "In what situation?"
│   ApplicabilityConstraint.java # "Does the product fit this situation?"
│
├── relationship/             # Relationships between products as standalone entities
│   ProductRelationship.java
│   ProductRelationshipFactory.java
│   ProductRelationshipPolicy.java  # + NoSelfRelationshipPolicy
│
├── bundle/                   # Packages — composition of products and selection rules
│   PackageType.java          # Package definition (implements Product)
│   PackageStructure.java
│   ProductSet.java           # Group of options to choose from
│   SelectionRule.java        # Rules: single, required, isSubsetOf, ifThen
│   PackageValidationResult.java
│   SelectedProduct.java
│
└── escaperoom/               # Domain layer — the concrete offering
    EscapeRoomCatalog.java    # Rooms, add-ons, packages, relationships
```

---

## Patterns used

### 1. Type–Instance — definition versus occurrence

> **Pattern M02 §3** — separate `ProductType` from `ProductInstance`.

| Class             | Role                                                            |
| ----------------- | --------------------------------------------------------------- |
| `ProductType`     | Definition: what a room *is* — name, metadata, features, rules |
| `ProductInstance` | Booking: a concrete group visit with a reference number         |

Changing the offering (e.g. shortening a room's duration) does not mutate the history of existing bookings.

```java
// definition — a single object for the entire catalog
public static final ProductType MAD_SCIENTIST_LAB = ProductType.builder(...).build();

// instance — created on every booking
ProductInstance booking = ProductInstance.of(
    EscapeRoomCatalog.MAD_SCIENTIST_LAB,
    SerialNumber.of("BOOKING-2025-03-15-NOWAK")
);
```

Files: `product/ProductType.java`, `product/ProductInstance.java`

---

### 2. Composite — products and packages behind a single interface

> **Pattern M02 §14** — `ProductType` and `PackageType` implement the shared contract `Product`.

```text
          «interface»
            Product
           /        \
    ProductType   PackageType
```

The catalog, search engine, and relationship graph all operate on `Product` — they don't know whether they are
dealing with a single room or a composite package. A package can contain products and, in the future, other
packages as well.

Files: `product/Product.java`, `bundle/PackageType.java`

---

### 3. Builder — readable construction of complex objects

> **GoF pattern** applied in `ProductType` and `PackageType`.

`ProductType.Builder` requires mandatory parameters in the constructor and adds features and constraints
optionally:

```java
ProductType.builder(id, name, desc, Unit.of("booking"), INDIVIDUALLY_TRACKED)
    .withMetadata(Map.of("difficulty", "hard", "durationMinutes", "75"))
    .withMandatoryFeature(ProductFeatureType.withNumericRange("participants", 3, 6))
    .withApplicabilityConstraint(not(eq("claustrophobia", "true")))
    .build();
```

`PackageType.Builder` assembles products and rules without a list of fields bolted onto a base class:

```java
PackageType.builder(id, name, desc)
    .withProductSet(rooms)
    .withSelectionRule(SelectionRule.isSubsetOf(rooms, 2, 2))
    .withSelectionRule(SelectionRule.required(catering))
    .build();
```

Files: `product/ProductType.java` (inner `Builder`), `bundle/PackageType.java` (inner `Builder`)

---

### 4. Feature model + Constraint (Specification)

> **Pattern M02 §5, §6** — features and their semantics are separated from the class structure.

Instead of table columns and class fields: `ProductFeatureType` defines what can be configured;
`FeatureValueConstraint` guards the correctness of values. The domain rejects invalid data by itself —
no controller is needed.

| Constraint      | Domain application                                          |
| --------------- | ----------------------------------------------------------- |
| `AllowedValues` | Catering variant: `"pizza"` \| `"sushi"` \| `"vegetarian"` |
| `NumericRange`  | Number of participants within room capacity (e.g. 2–5)     |

```java
// declaring a feature
ProductFeatureType.withAllowedValues("variant", "pizza", "sushi", "vegetarian")
ProductFeatureType.withNumericRange("participants", 2, 5)

// validating a value
variantFeature.isValid("hamburger")   // → false  (rule in the model, not in a controller)
participantsFeature.isValid(6)        // → false  (exceeds max 5)
```

Files: `product/feature/FeatureValueConstraint.java`, `product/feature/ProductFeatureType.java`,
`product/feature/ProductFeatureTypes.java`

---

### 5. Metadata vs Feature — identity versus configuration

> **Pattern M02 §7** — separate what defines a product from what can be changed in an instance.

| Attribute                            | Kind         | Rationale                                    |
| ------------------------------------ | ------------ | -------------------------------------------- |
| `difficulty`, `durationMinutes`      | **Metadata** | Changing it would mean a different room      |
| `minParticipants`, `maxParticipants` | **Metadata** | Built into the room's physical design        |
| `requiresVr`                         | **Metadata** | Equipment determines product identity        |
| `participants` (how many people)     | **Feature**  | Customer's choice on each booking            |
| `variant` (pizza/sushi/vegetarian)   | **Feature**  | Customer's choice when ordering catering     |

Files: `product/ProductMetadata.java`, `product/feature/ProductFeatureType.java`

---

### 6. Tracking Strategy — instance tracking strategy

> **Pattern M02 §3, §4** — `ProductTrackingStrategy` is part of the definition and is enforced when creating instances.

Every booking of a room or add-on is a distinct event requiring a reference number →
`INDIVIDUALLY_TRACKED`. The `ProductInstance` constructor rejects creation without a `SerialNumber`:

```java
// throws IllegalArgumentException — the strategy does real work, it is not just metadata
ProductInstance.of(EscapeRoomCatalog.MAD_SCIENTIST_LAB, null);

// correct instance creation
ProductInstance.of(alcatraz, SerialNumber.of("BOOKING-2025-01-KOWALSKI"));
```

Files: `product/ProductInstance.java`, `product/ProductTrackingStrategy.java`

---

### 7. Sealed interface + Composite Specification — applicability rules

> **Pattern M02 §11** — `ApplicabilityConstraint` answers the question "does the product fit this situation?"

`ApplicabilityConstraint` is a sealed interface with nested records:
`eq`, `in`, `greaterThan`, `and`, `or`, `not`. Rules can be composed into a tree of arbitrary depth.
The product itself answers `isApplicableFor(context)` — no logic in controllers or SQL filters.

```java
// Cyberpunk 2077: Warsaw only, only when VR equipment is present
and(eq("city", "Warsaw"), eq("hasVrEquipment", "true"))

// Hardcore package: adults only (age > 17)
greaterThan("age", 17)

// Alcatraz: unavailable for people with claustrophobia
not(eq("claustrophobia", "true"))

// Actor: available on weekends only
in("dayType", "Saturday", "Sunday")
```

`ApplicabilityContext` is a flat `String → String` map that travels across service boundaries
and can be built from HTTP headers, JWT claims, or a booking form.

Files: `applicability/ApplicabilityConstraint.java`, `applicability/ApplicabilityContext.java`

---

### 8. Relationship as a first-class entity + Factory + Policy

> **Pattern M02 §9, §10** — relationships are not fields in `ProductType` but independent entities with their own ID.

`ProductRelationship` lives independently. `ProductRelationshipFactory` verifies domain policies
before every relationship is created — rules are always enforced in one place.

```java
// difficulty path: easy → extreme
factory.define(ID_EGYPTIAN_TOMB,     ID_MAD_SCIENTIST_LAB, UPGRADABLE_TO)
factory.define(ID_MAD_SCIENTIST_LAB, ID_ALCATRAZ,          UPGRADABLE_TO)
factory.define(ID_ALCATRAZ,          ID_CYBERPUNK_2077,     UPGRADABLE_TO)

// rooms complemented by add-ons
factory.define(ID_ALCATRAZ, ID_ACTOR,    COMPLEMENTED_BY)
factory.define(ID_ALCATRAZ, ID_CATERING, COMPLEMENTED_BY)

// policy rejects a product relating to itself
factory.define(ID_ALCATRAZ, ID_ALCATRAZ, UPGRADABLE_TO)  // → IllegalArgumentException
```

`NoSelfRelationshipPolicy` demonstrates the Policy pattern — a single domain rule in a single
place. In production a `NoCyclicUpgradePolicy` backed by a repository query would be added here.

Files: `relationship/ProductRelationship.java`, `relationship/ProductRelationshipFactory.java`,
`relationship/ProductRelationshipPolicy.java`, `relationship/NoSelfRelationshipPolicy.java`

---

### 9. Package = composition of rules, not a list of fields

> **Pattern M02 §13, §14** — a package is `PackageType` + `ProductSet` + `SelectionRule`, not fields bolted onto a product.

The **Team Building** package consists of three sets and rules that must all be satisfied simultaneously:

| ProductSet            | SelectionRule             | Meaning                                    |
| --------------------- | ------------------------- | ------------------------------------------ |
| `Rooms` (4 rooms)     | `isSubsetOf(rooms, 2, 2)` | Exactly 2 rooms (played sequentially)      |
| `Catering`            | `required(catering)`      | Catering mandatory                         |
| `DedicatedGameMaster` | `required(dedicatedGm)`   | Dedicated GM mandatory                     |

```java
PackageValidationResult result = EscapeRoomCatalog.TEAM_BUILDING.validateSelection(selection);
result.isValid();   // true / false
result.errors();    // list of violated rules
```

Validation operates at the type level (`SelectedProduct`) without needing to create instances —
simple and sufficient for this domain scope.

Files: `bundle/PackageType.java`, `bundle/ProductSet.java`, `bundle/SelectionRule.java`,
`bundle/PackageStructure.java`, `escaperoom/EscapeRoomCatalog.java`

---

## Answers to the 5 task points

### Point 1 — Room definitions (`ProductType`)

Four rooms as static constants in `EscapeRoomCatalog`:
`MAD_SCIENTIST_LAB`, `ALCATRAZ`, `EGYPTIAN_TOMB`, `CYBERPUNK_2077`.

- **Metadata** (identity): `difficulty`, `durationMinutes`, `minParticipants`, `maxParticipants`, `requiresVr`
- **Feature** (configuration): `participants` — `NumericRange` aligned with room capacity
- **Tracking**: `INDIVIDUALLY_TRACKED` — every booking = a separate instance with a reference number
- **Applicability**: rooms default to `alwaysTrue`; exceptions: Cyberpunk (Warsaw + VR), Alcatraz (no claustrophobia)

Tests: `RoomProductTypeTest`, `ProductInstanceTrackingTest`

### Point 2 — Add-ons and the catering decision

Four add-ons: `ACTOR`, `PHOTO_VIDEO`, `CATERING`, `DEDICATED_GM`.

**Catering is a single `ProductType` with a `variant` feature (AllowedValues: pizza/sushi/vegetarian).**

Rationale: pizza, sushi, and the vegetarian option share the same tracking strategy, the same
operational lifecycle (order → preparation → served after the game), the same applicability rules,
and the same pricing structure. The only difference is the dish choice — that is a `Feature`, not
a structural distinction. If pricing or availability were to diverge per variant, we would promote
them to separate `ProductType` instances without touching the rest of the code.

Tests: `AddOnProductTypeTest`

### Point 3 — Team Building package (`PackageType`)

```text
TeamBuilding
  ├── ProductSet "Rooms"              → isSubsetOf(2, 2)   — exactly 2 rooms
  ├── ProductSet "Catering"           → required           — catering mandatory
  └── ProductSet "DedicatedGameMaster"→ required           — dedicated GM mandatory
```

Dependencies: no conditional rules (`ifThen`) — all three sets are unconditionally required.
The sequential nature of the two rooms is an operational detail passed to fulfilment via description —
the catalog defines *what*, not *when*.

Tests: `TeamBuildingPackageTest`

### Point 4 — `ApplicabilityConstraint`

| Product/package | Constraint                                              | Context                          |
| --------------- | ------------------------------------------------------- | -------------------------------- |
| Cyberpunk 2077  | `and(eq("city","Warsaw"), eq("hasVrEquipment","true"))` | VR equipment available in Warsaw only |
| Hardcore package | `greaterThan("age", 17)`                               | Adults (18+) only                |
| Alcatraz        | `not(eq("claustrophobia","true"))`                      | Self-declaration of claustrophobia |
| Actor           | `in("dayType", "Saturday", "Sunday")`                   | Weekends only                    |

Tests: `ApplicabilityConstraintTest`

### Point 5 — `ProductRelationship`

**Difficulty path** (`UPGRADABLE_TO`, directional):
`Egyptian Tomb (easy) → Mad Scientist's Lab (medium) → Alcatraz (hard) → Cyberpunk 2077 (extreme)`

The `UPGRADABLE_TO` relationship is directional: "Alcatraz → Cyberpunk" does not imply the reverse.
The catalog can use this relationship to suggest the "next challenge" after completing a room.

**Complementarity** (`COMPLEMENTED_BY`): every room → actor, catering, photo/video.
The relationship drives add-on suggestions during booking with no conditional code.

**Incompatible, compatible products**: not modelled — no explicit domain requirement;
`INCOMPATIBLE_WITH` and `COMPATIBLE_WITH` are declared in the enum and ready to use.

Tests: `ProductRelationshipTest`

---

## Test map

| Test file                     | Task point | What it demonstrates                                        |
| ----------------------------- | ---------- | ----------------------------------------------------------- |
| `RoomProductTypeTest`         | 1          | Metadata, feature constraints, tracking strategy            |
| `ProductInstanceTrackingTest` | 1          | `INDIVIDUALLY_TRACKED` requires `SerialNumber`              |
| `AddOnProductTypeTest`        | 2          | Catering = 1 product with variant; AllowedValues            |
| `TeamBuildingPackageTest`     | 3          | Package selection validation (valid / invalid)              |
| `ApplicabilityConstraintTest` | 4          | Cyberpunk, Hardcore, Alcatraz, actor                        |
| `ProductRelationshipTest`     | 5          | Upgrade path, complemented-by, factory policy               |

---

## Deliberate simplifications (`// ponytail:`)

| Omitted                                          | Where the note lives                           | When to add                                                                      |
| ------------------------------------------------ | ---------------------------------------------- | -------------------------------------------------------------------------------- |
| `CatalogEntry` (campaigns, sales channels)       | `product/Product.java`                         | When the same definition needs different prices / marketing copy per channel     |
| `PackageInstance` + `SelectedInstance`           | `bundle/PackageType.java`                      | When full validation at the concrete-instance level with serial numbers is needed |
| `BATCH_TRACKED`, `QUANTITY_TRACKED`              | `product/ProductTrackingStrategy.java`         | Products like "escape room tea supply"                                           |
| `DecimalRange`, `DateRange`, `Regex` constraints | `product/feature/FeatureValueConstraint.java`  | Prices, booking dates, voucher codes                                             |
| `LessThan`, `Between` in applicability           | `applicability/ApplicabilityConstraint.java`   | Rules like "age < 12 = child ticket"                                             |
| `SUBSTITUTED_BY`, `REPLACED_BY` in relationships | `relationship/ProductRelationshipType.java`    | Retiring a room and replacing it with a new one                                  |
| `NoCyclicUpgradePolicy`                          | `relationship/ProductRelationshipFactory.java` | When the upgrade graph is large and the cycle risk is real                       |
