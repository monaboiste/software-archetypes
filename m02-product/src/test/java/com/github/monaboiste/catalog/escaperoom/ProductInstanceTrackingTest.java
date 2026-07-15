package com.github.monaboiste.catalog.escaperoom;

import com.github.monaboiste.catalog.product.ProductInstance;
import com.github.monaboiste.catalog.product.SerialNumber;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Task point 1 — lightweight ProductInstance layer.
 *
 * <p>Shows that the tracking strategy does real work: INDIVIDUALLY_TRACKED products require a
 * serial number (booking reference) and reject creation without one.
 */
class ProductInstanceTrackingTest {

    @Test
    void individually_tracked_room_requires_serial_number() {
        // The strategy is not just metadata — it enforces an invariant at construction time.
        assertThatThrownBy(() -> ProductInstance.of(EscapeRoomCatalog.MAD_SCIENTIST_LAB, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INDIVIDUALLY_TRACKED");
    }

    @Test
    void booking_instance_is_created_with_valid_serial() {
        SerialNumber bookingRef = SerialNumber.of("BOOKING-2025-03-15-NOWAK");

        ProductInstance instance = ProductInstance.of(
                EscapeRoomCatalog.MAD_SCIENTIST_LAB,
                bookingRef);

        assertThat(instance.productType()).isEqualTo(EscapeRoomCatalog.MAD_SCIENTIST_LAB);
        assertThat(instance.serialNumber()).isEqualTo(bookingRef);
        assertThat(instance.id()).isNotNull();
    }

    @Test
    void two_bookings_of_the_same_room_produce_distinct_instances() {
        // Each booking is a separate event — different ProductInstanceId even for the same room.
        ProductInstance first = ProductInstance.of(
                EscapeRoomCatalog.ALCATRAZ,
                SerialNumber.of("BOOKING-2025-01-KOWALSKI"));
        ProductInstance second = ProductInstance.of(
                EscapeRoomCatalog.ALCATRAZ,
                SerialNumber.of("BOOKING-2025-02-NOWAK"));

        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(first.productType()).isEqualTo(second.productType());
    }
}
