package com.github.monaboiste.catalog.product;

import java.util.UUID;

/**
 * Technical identifier of a product instance.
 */
public record ProductInstanceId(String value) {

    public static ProductInstanceId of(String value) {
        return new ProductInstanceId(value);
    }

    public static ProductInstanceId newOne() {
        return new ProductInstanceId(UUID.randomUUID().toString());
    }
}
