ALTER TABLE users ADD CONSTRAINT chk_role CHECK (role IN ('CAMPER','MERCHANT','MERCHANT_ADMIN','MERCHANT_MEMBER','SUPERADMIN','ADMIN'));
ALTER TABLE users ADD CONSTRAINT chk_status CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED'));
ALTER TABLE users ADD CONSTRAINT chk_business_name CHECK ((role IN ('MERCHANT','MERCHANT_ADMIN','MERCHANT_MEMBER') AND business_name IS NOT NULL) OR (role NOT IN ('MERCHANT','MERCHANT_ADMIN','MERCHANT_MEMBER')));

ALTER TABLE campsites ADD CONSTRAINT chk_rating CHECK (rating >= 0.0 AND rating <= 5.0);
ALTER TABLE campsites ADD CONSTRAINT chk_campsite_status CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED'));
ALTER TABLE campsites ADD CONSTRAINT chk_coordinates CHECK ((latitude IS NULL AND longitude IS NULL) OR (latitude IS NOT NULL AND longitude IS NOT NULL));

ALTER TABLE products ADD CONSTRAINT chk_product_type CHECK (type IN ('RENTAL_SPOT','RENTAL_ITEM','SALE'));
ALTER TABLE products ADD CONSTRAINT chk_product_status CHECK (status IN ('ACTIVE','INACTIVE','ARCHIVED'));
ALTER TABLE products ADD CONSTRAINT chk_base_price CHECK (base_price >= 0);
ALTER TABLE products ADD CONSTRAINT chk_rental_fields CHECK ((type IN ('RENTAL_SPOT','RENTAL_ITEM') AND stock_total IS NOT NULL AND daily_rate IS NOT NULL) OR (type NOT IN ('RENTAL_SPOT','RENTAL_ITEM') AND stock_total IS NULL AND daily_rate IS NULL));
ALTER TABLE products ADD CONSTRAINT chk_sale_fields CHECK ((type = 'SALE' AND current_stock IS NOT NULL AND unit_price IS NOT NULL) OR (type != 'SALE' AND current_stock IS NULL AND unit_price IS NULL));

ALTER TABLE map_configurations ADD CONSTRAINT chk_map_status CHECK (status IN ('DRAFT','ACTIVE','ARCHIVED'));
ALTER TABLE map_configurations ADD CONSTRAINT chk_image_dimensions CHECK (image_width > 0 AND image_height > 0);

ALTER TABLE bundles ADD CONSTRAINT chk_bundle_status CHECK (status IN ('ACTIVE','INACTIVE','ARCHIVED'));
ALTER TABLE bundles ADD CONSTRAINT chk_bundle_price CHECK (bundle_price >= 0);

ALTER TABLE bookings ADD CONSTRAINT chk_booking_status CHECK (status IN ('IN_CART','PAYMENT_PENDING','CONFIRMED','CANCELLED','COMPLETED'));
ALTER TABLE bookings ADD CONSTRAINT chk_booking_dates CHECK (check_out_date > check_in_date);

ALTER TABLE inventory_locks ADD CONSTRAINT chk_inventory_dates CHECK ((start_date IS NULL AND end_date IS NULL) OR (start_date IS NOT NULL AND end_date IS NOT NULL AND end_date > start_date));

ALTER TABLE reviews ADD CONSTRAINT chk_review_rating CHECK (rating >= 1 AND rating <= 5);
ALTER TABLE reviews ADD CONSTRAINT chk_review_status CHECK (status IN ('ACTIVE','HIDDEN','DELETED'));

ALTER TABLE seasonal_pricing_rules ADD CONSTRAINT chk_adjustment_type CHECK (adjustment_type IN ('PERCENTAGE','FIXED_AMOUNT'));
ALTER TABLE seasonal_pricing_rules ADD CONSTRAINT chk_date_range CHECK (end_date >= start_date);
ALTER TABLE seasonal_pricing_rules ADD CONSTRAINT chk_min_max_price CHECK (max_price IS NULL OR min_price IS NULL OR max_price >= min_price);

ALTER TABLE day_of_week_pricing ADD CONSTRAINT chk_day_of_week CHECK (day_of_week BETWEEN 1 AND 7);
ALTER TABLE day_of_week_pricing ADD CONSTRAINT chk_dow_adjustment_type CHECK (adjustment_type IN ('PERCENTAGE','FIXED_AMOUNT'));

ALTER TABLE special_date_pricing ADD CONSTRAINT chk_special_adjustment_type CHECK (adjustment_type IN ('PERCENTAGE','FIXED_AMOUNT'));

ALTER TABLE promo_codes ADD CONSTRAINT chk_promo_discount_type CHECK (discount_type IN ('PERCENTAGE','FIXED_AMOUNT'));
ALTER TABLE promo_codes ADD CONSTRAINT chk_promo_discount_value CHECK (discount_value > 0);
ALTER TABLE promo_codes ADD CONSTRAINT chk_promo_min_amount CHECK (min_order_amount IS NULL OR min_order_amount >= 0);
ALTER TABLE promo_codes ADD CONSTRAINT chk_promo_max_amount CHECK (max_discount_amount IS NULL OR max_discount_amount >= 0);
ALTER TABLE promo_codes ADD CONSTRAINT chk_promo_limits CHECK (total_usage_limit IS NULL OR total_usage_limit >= 0);

ALTER TABLE notifications ADD CONSTRAINT chk_notification_type CHECK (type IN ('BOOKING_CREATED','BOOKING_CONFIRMED','BOOKING_CANCELLED','BOOKING_REMINDER','PAYMENT_SUCCESS','PAYMENT_FAILED','PAYMENT_REFUNDED','REVIEW_REQUEST','REVIEW_RESPONSE','PROMO_CODE','SYSTEM_ANNOUNCEMENT','MERCHANT_NEW_BOOKING','MERCHANT_PAYOUT','MERCHANT_LOW_STOCK'));
ALTER TABLE notifications ADD CONSTRAINT chk_notification_priority CHECK (priority IN ('LOW','NORMAL','HIGH','URGENT'));

ALTER TABLE email_logs ADD CONSTRAINT chk_email_status CHECK (status IN ('PENDING','QUEUED','SENT','DELIVERED','OPENED','CLICKED','BOUNCED','FAILED','SPAM'));

ALTER TABLE push_notification_tokens ADD CONSTRAINT chk_device_type CHECK (device_type IN ('IOS','ANDROID','WEB'));

ALTER TABLE push_notification_logs ADD CONSTRAINT chk_push_status CHECK (status IN ('PENDING','SENT','DELIVERED','FAILED','INVALID_TOKEN'));

ALTER TABLE system_announcements ADD CONSTRAINT chk_announcement_type CHECK (type IN ('INFO','WARNING','MAINTENANCE','PROMOTION','UPDATE'));
ALTER TABLE system_announcements ADD CONSTRAINT chk_announcement_audience CHECK (target_audience IN ('ALL','CAMPERS','MERCHANTS','ADMINS'));
ALTER TABLE system_announcements ADD CONSTRAINT chk_announcement_dates CHECK (display_until > display_from);

ALTER TABLE booking_reminders ADD CONSTRAINT chk_reminder_type CHECK (reminder_type IN ('24H','3H','1H','IMMEDIATE'));

ALTER TABLE payment_transactions ADD CONSTRAINT chk_payment_status CHECK (status IN ('PENDING','AWAITING_PAYMENT','PROCESSING','SUCCESS','FAILED','EXPIRED','CANCELLED','REFUNDED','PARTIAL_REFUND'));
ALTER TABLE payment_transactions ADD CONSTRAINT chk_payment_amount CHECK (amount > 0);
ALTER TABLE payment_transactions ADD CONSTRAINT chk_net_amount CHECK (net_amount IS NULL OR net_amount >= 0);

ALTER TABLE payment_refunds ADD CONSTRAINT chk_refund_status CHECK (status IN ('PENDING','PROCESSING','COMPLETED','FAILED','REJECTED'));
ALTER TABLE payment_refunds ADD CONSTRAINT chk_refund_type CHECK (refund_type IN ('FULL','PARTIAL'));
ALTER TABLE payment_refunds ADD CONSTRAINT chk_refund_reason CHECK (reason IN ('CUSTOMER_REQUEST','BOOKING_CANCELLED','DUPLICATE_PAYMENT','SERVICE_ISSUE','ADMIN_INITIATED','OTHER'));
ALTER TABLE payment_refunds ADD CONSTRAINT chk_refund_amount CHECK (refund_amount > 0 AND refund_amount <= original_amount);

ALTER TABLE payment_webhook_logs ADD CONSTRAINT chk_webhook_processed CHECK ((processed = TRUE AND processed_at IS NOT NULL) OR (processed = FALSE));
