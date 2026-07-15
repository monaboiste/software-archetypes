package com.github.monaboiste.catalog.relationship;

/**
 * Types of explicit, directed relationships between products.
 * Relationships are first-class entities — not fields on
 * {@link com.github.monaboiste.catalog.product.ProductType}. Keeping them separate means the
 * product model stays lean and the relationship graph can grow without touching product
 * definitions.
 *
 * <p>// ponytail: SUBSTITUTED_BY, REPLACED_BY omitted — no deprecation scenario in the current
 * escape-room domain. Add as enum constants when a room is retired and replaced.
 */
public enum ProductRelationshipType {
    /**
     * Source can be upgraded to target (e.g. easier room → harder room).
     */
    UPGRADABLE_TO,
    /**
     * Source is enhanced by target (e.g. room is complemented by an add-on).
     */
    COMPLEMENTED_BY,
    /**
     * Source and target can be used together.
     */
    COMPATIBLE_WITH,
    /**
     * Source and target cannot coexist in the same booking or package.
     */
    INCOMPATIBLE_WITH
}
