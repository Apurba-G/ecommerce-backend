package com.ecommerce.common.constant;

public final class EventConstants {

    private EventConstants() {}

    public static final String AUTH_EXCHANGE = "auth.exchange";
    public static final String CATALOG_EXCHANGE = "catalog.exchange";

    public static final String USER_REGISTERED_KEY = "user.registered";
    public static final String PRODUCT_CREATED_KEY = "product.created";
    public static final String PRODUCT_UPDATED_KEY = "product.updated";
    public static final String PRODUCT_DELETED_KEY = "product.deleted";

    public static final String USER_PROFILE_QUEUE = "user.profile.sync.queue";
    public static final String CATALOG_INDEX_QUEUE = "catalog.search.sync.queue";
}
