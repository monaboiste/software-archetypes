package com.github.monaboiste.catalog.escaperoom;

import com.github.monaboiste.catalog.product.ProductInstance;
import com.github.monaboiste.catalog.product.SerialNumber;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Task point 1 — tracking strategy enforcement on ProductInstance.
 *
 * <p>Shows that the tracking strategy does real work: INDIVIDUALLY_TRACKED products require a
 * serial number (booking reference) and reject creation without one.
 *
 * <p>Uses feature-less add-on types (Photo/Video, Dedicated GM) because these tests focus on
 * tracking, not on feature validation. Feature validation is covered in
 * {@link ProductInstanceFeatureTest}.
 */
class ProductInstanceTrackingTest {

    @Test
    void individually_tracked_product_requires_serial_number() {
        // The strategy is not just metadata — it enforces an invariant at construction time.
        // PHOTO_VIDEO has no mandatory features, so only the tracking guard fires.
        assertThatThrownBy(() -> ProductInstance.of(EscapeRoomCatalog.PHOTO_VIDEO, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INDIVIDUALLY_TRACKED");
    }

    @Test
    void booking_instance_is_created_with_valid_serial() {
        SerialNumber sessionRef = SerialNumber.of("SESSION-2025-03-15-NOWAK");

        ProductInstance instance = ProductInstance.of(
                EscapeRoomCatalog.PHOTO_VIDEO,
                sessionRef);

        assertThat(instance.productType()).isEqualTo(EscapeRoomCatalog.PHOTO_VIDEO);
        assertThat(instance.serialNumber()).isEqualTo(sessionRef);
        assertThat(instance.id()).isNotNull();
    }

    @Test
    void two_bookings_of_the_same_product_produce_distinct_instances() {
        // Each session is a separate event — different ProductInstanceId even for the same product.
        ProductInstance first = ProductInstance.of(
                EscapeRoomCatalog.DEDICATED_GM,
                SerialNumber.of("SESSION-2025-01-KOWALSKI"));
        ProductInstance second = ProductInstance.of(
                EscapeRoomCatalog.DEDICATED_GM,
                SerialNumber.of("SESSION-2025-02-NOWAK"));

        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(first.productType()).isEqualTo(second.productType());
    }
}
