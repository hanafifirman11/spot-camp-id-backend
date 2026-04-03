-- Master data seed (idempotent). Update CAMPSITE_ID if needed.
-- Default target campsite_id = 21.

-- ==================== Amenities ====================
INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'atv', 'ATV', 'atv', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'atv');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'bbq_set', 'BBQ Set', 'bbq_set', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'bbq_set');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'bonfire', 'Bonfire', 'bonfire', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'bonfire');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'breakfast', 'Breakfast', 'breakfast', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'breakfast');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'campervan_spot', 'Campervan Spot', 'campervan_spot', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'campervan_spot');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'city_lights_view', 'City Lights View', 'city_lights_view', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'city_lights_view');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'city_view', 'City View', 'city_view', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'city_view');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'clean_toilets', 'Clean Toilets', 'clean_toilets', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'clean_toilets');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'comfortable_beds', 'Comfortable Beds', 'comfortable_beds', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'comfortable_beds');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'deer_feeding', 'Deer Feeding', 'deer_feeding', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'deer_feeding');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'easy_access', 'Easy Access', 'easy_access', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'easy_access');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'electricity', 'Electricity', 'electricity', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'electricity');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'electricity_hookup', 'Electricity Hookup', 'electricity_hookup', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'electricity_hookup');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'extreme_track', 'Extreme Track', 'extreme_track', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'extreme_track');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'flying_fox', 'Flying Fox', 'flying_fox', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'flying_fox');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'food_stalls', 'Food Stalls', 'food_stalls', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'food_stalls');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'full_board_meals', 'Full Board Meals', 'full_board_meals', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'full_board_meals');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'golf_view', 'Golf View', 'golf_view', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'golf_view');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'group_tent', 'Group Tent', 'group_tent', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'group_tent');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'guitar_rental', 'Guitar Rental', 'guitar_rental', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'guitar_rental');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'hammock_rental', 'Hammock Rental', 'hammock_rental', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'hammock_rental');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'hammock_spot', 'Hammock Spot', 'hammock_spot', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'hammock_spot');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'hobbit_houses', 'Hobbit Houses', 'hobbit_houses', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'hobbit_houses');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'hot_shower', 'Hot Shower', 'hot_shower', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'hot_shower');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'hot_spring_pool', 'Hot Spring Pool', 'hot_spring_pool', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'hot_spring_pool');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'hot_water', 'Hot Water', 'hot_water', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'hot_water');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'karaoke', 'Karaoke', 'karaoke', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'karaoke');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'lake_view', 'Lake View', 'lake_view', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'lake_view');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'large_field', 'Large Field', 'large_field', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'large_field');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'manual_brew_coffee', 'Manual Brew Coffee', 'manual_brew_coffee', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'manual_brew_coffee');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'mosque', 'Mosque', 'mosque', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'mosque');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'national_park_access', 'National Park Access', 'national_park_access', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'national_park_access');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'outbound_area', 'Outbound Area', 'outbound_area', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'outbound_area');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'paintball', 'Paintball', 'paintball', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'paintball');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'parking', 'Parking', 'parking', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'parking');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'private_area', 'Private Area', 'private_area', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'private_area');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'private_pool', 'Private Pool', 'private_pool', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'private_pool');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'rabbit_park', 'Rabbit Park', 'rabbit_park', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'rabbit_park');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'rafting', 'Rafting', 'rafting', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'rafting');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'restaurant', 'Restaurant', 'restaurant', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'restaurant');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'river', 'River', 'river', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'river');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'river_access', 'River Access', 'river_access', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'river_access');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'room_service', 'Room Service', 'room_service', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'room_service');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'security', 'Security', 'security', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'security');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'souvenir_shop', 'Souvenir Shop', 'souvenir_shop', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'souvenir_shop');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'strawberry_picking', 'Strawberry Picking', 'strawberry_picking', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'strawberry_picking');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'sunrise_view', 'Sunrise View', 'sunrise_view', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'sunrise_view');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'sunset_view', 'Sunset View', 'sunset_view', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'sunset_view');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'suspension_bridge', 'Suspension Bridge', 'suspension_bridge', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'suspension_bridge');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'swimming_pool', 'Swimming Pool', 'swimming_pool', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'swimming_pool');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'tea_garden', 'Tea Garden', 'tea_garden', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'tea_garden');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'tea_walk', 'Tea Walk', 'tea_walk', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'tea_walk');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'toilet', 'Toilet', 'toilet', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'toilet');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'trekking_guide', 'Trekking Guide', 'trekking_guide', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'trekking_guide');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'warung', 'Warung', 'warung', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'warung');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'water_park', 'Water Park', 'water_park', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'water_park');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'waterfall', 'Waterfall', 'waterfall', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'waterfall');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'wifi', 'WiFi', 'wifi', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'wifi');

INSERT INTO amenities (code, name, icon, category, is_active)
SELECT 'wooden_bridge', 'Wooden Bridge', 'wooden_bridge', 'general', TRUE
WHERE NOT EXISTS (SELECT 1 FROM amenities WHERE code = 'wooden_bridge');

-- ==================== Products (per campsite) ====================
INSERT INTO products (campsite_id, name, description, type, category, item_type, base_price, status, stock_total, buffer_time_minutes, daily_rate)
SELECT c.id, 'Riverside Pitch A', 'Riverside tent pitch with easy access.', 'RENTAL_SPOT', 'RENTAL', 'SPOT', 250000, 'ACTIVE', 1, 120, 250000
FROM campsites c
WHERE NOT EXISTS (
  SELECT 1 FROM products p WHERE p.campsite_id = c.id AND p.name = 'Riverside Pitch A'
);

INSERT INTO products (campsite_id, name, description, type, category, item_type, base_price, status, stock_total, buffer_time_minutes, daily_rate)
SELECT c.id, 'Forest Tent B', 'Shaded forest tent area for couples.', 'RENTAL_SPOT', 'RENTAL', 'SPOT', 300000, 'ACTIVE', 1, 120, 300000
FROM campsites c
WHERE NOT EXISTS (
  SELECT 1 FROM products p WHERE p.campsite_id = c.id AND p.name = 'Forest Tent B'
);

INSERT INTO products (campsite_id, name, description, type, category, item_type, base_price, status, stock_total, buffer_time_minutes, daily_rate)
SELECT c.id, 'Pine Glamp C', 'Glamping tent with bedding and lamp.', 'RENTAL_SPOT', 'RENTAL', 'TENT', 450000, 'ACTIVE', 1, 120, 450000
FROM campsites c
WHERE NOT EXISTS (
  SELECT 1 FROM products p WHERE p.campsite_id = c.id AND p.name = 'Pine Glamp C'
);

INSERT INTO products (campsite_id, name, description, type, category, item_type, base_price, status, stock_total, buffer_time_minutes, daily_rate)
SELECT c.id, 'Lake View Deck D', 'Platform deck overlooking the lake.', 'RENTAL_SPOT', 'RENTAL', 'SPOT', 280000, 'ACTIVE', 1, 120, 280000
FROM campsites c
WHERE NOT EXISTS (
  SELECT 1 FROM products p WHERE p.campsite_id = c.id AND p.name = 'Lake View Deck D'
);

INSERT INTO products (campsite_id, name, description, type, category, item_type, base_price, status, stock_total, buffer_time_minutes, daily_rate)
SELECT c.id, 'Hilltop Cabin E', 'Private cabin with elevated view.', 'RENTAL_SPOT', 'RENTAL', 'SPOT', 500000, 'ACTIVE', 1, 120, 500000
FROM campsites c
WHERE NOT EXISTS (
  SELECT 1 FROM products p WHERE p.campsite_id = c.id AND p.name = 'Hilltop Cabin E'
);

INSERT INTO products (campsite_id, name, description, type, category, item_type, base_price, status, stock_total, buffer_time_minutes, daily_rate)
SELECT c.id, 'Family Ground F', 'Large flat ground for family tents.', 'RENTAL_SPOT', 'RENTAL', 'SPOT', 320000, 'ACTIVE', 1, 120, 320000
FROM campsites c
WHERE NOT EXISTS (
  SELECT 1 FROM products p WHERE p.campsite_id = c.id AND p.name = 'Family Ground F'
);

INSERT INTO products (campsite_id, name, description, type, category, item_type, base_price, status, stock_total, buffer_time_minutes, daily_rate)
SELECT c.id, 'Campervan Slot G', 'Dedicated campervan slot with power.', 'RENTAL_SPOT', 'RENTAL', 'SPOT', 220000, 'ACTIVE', 1, 120, 220000
FROM campsites c
WHERE NOT EXISTS (
  SELECT 1 FROM products p WHERE p.campsite_id = c.id AND p.name = 'Campervan Slot G'
);

INSERT INTO products (campsite_id, name, description, type, category, item_type, base_price, status, stock_total, buffer_time_minutes, daily_rate)
SELECT c.id, 'Hammock Grove H', 'Hammock area under pine trees.', 'RENTAL_SPOT', 'RENTAL', 'SPOT', 180000, 'ACTIVE', 1, 120, 180000
FROM campsites c
WHERE NOT EXISTS (
  SELECT 1 FROM products p WHERE p.campsite_id = c.id AND p.name = 'Hammock Grove H'
);

INSERT INTO products (campsite_id, name, description, type, category, item_type, base_price, status, current_stock, reorder_level, unit_price)
SELECT c.id, 'Firewood Bundle', 'Dry firewood bundle (10 logs).', 'SALE', 'SALE', 'GOODS', 45000, 'ACTIVE', 50, 10, 45000
FROM campsites c
WHERE NOT EXISTS (
  SELECT 1 FROM products p WHERE p.campsite_id = c.id AND p.name = 'Firewood Bundle'
);

INSERT INTO products (campsite_id, name, description, type, category, item_type, base_price, status, current_stock, reorder_level, unit_price)
SELECT c.id, 'Camping Breakfast', 'Local breakfast set for one.', 'SALE', 'SALE', 'FNB', 35000, 'ACTIVE', 30, 8, 35000
FROM campsites c
WHERE NOT EXISTS (
  SELECT 1 FROM products p WHERE p.campsite_id = c.id AND p.name = 'Camping Breakfast'
);

INSERT INTO products (campsite_id, name, description, type, category, item_type, base_price, status, current_stock, reorder_level, unit_price)
SELECT c.id, 'BBQ Kit', 'Grill set with charcoal and tools.', 'SALE', 'SALE', 'GOODS', 75000, 'ACTIVE', 20, 6, 75000
FROM campsites c
WHERE NOT EXISTS (
  SELECT 1 FROM products p WHERE p.campsite_id = c.id AND p.name = 'BBQ Kit'
);

-- ==================== Bundles (per campsite) ====================
INSERT INTO bundles (campsite_id, name, description, bundle_price, status)
SELECT c.id, 'Family Weekend Pack', 'Family ground + breakfast + firewood.', 399000, 'ACTIVE'
FROM campsites c
WHERE NOT EXISTS (
  SELECT 1 FROM bundles b WHERE b.campsite_id = c.id AND b.name = 'Family Weekend Pack'
);

INSERT INTO bundles (campsite_id, name, description, bundle_price, status)
SELECT c.id, 'Couple Escape Pack', 'Riverside pitch + BBQ kit + firewood.', 329000, 'ACTIVE'
FROM campsites c
WHERE NOT EXISTS (
  SELECT 1 FROM bundles b WHERE b.campsite_id = c.id AND b.name = 'Couple Escape Pack'
);

-- Bundle components
INSERT INTO bundle_components (bundle_id, product_id, quantity)
SELECT b.id, p.id, 1
FROM bundles b
JOIN products p ON p.campsite_id = b.campsite_id AND p.name = 'Family Ground F'
WHERE b.name = 'Family Weekend Pack'
  AND NOT EXISTS (
    SELECT 1 FROM bundle_components bc WHERE bc.bundle_id = b.id AND bc.product_id = p.id
  );

INSERT INTO bundle_components (bundle_id, product_id, quantity)
SELECT b.id, p.id, 4
FROM bundles b
JOIN products p ON p.campsite_id = b.campsite_id AND p.name = 'Camping Breakfast'
WHERE b.name = 'Family Weekend Pack'
  AND NOT EXISTS (
    SELECT 1 FROM bundle_components bc WHERE bc.bundle_id = b.id AND bc.product_id = p.id
  );

INSERT INTO bundle_components (bundle_id, product_id, quantity)
SELECT b.id, p.id, 2
FROM bundles b
JOIN products p ON p.campsite_id = b.campsite_id AND p.name = 'Firewood Bundle'
WHERE b.name = 'Family Weekend Pack'
  AND NOT EXISTS (
    SELECT 1 FROM bundle_components bc WHERE bc.bundle_id = b.id AND bc.product_id = p.id
  );

INSERT INTO bundle_components (bundle_id, product_id, quantity)
SELECT b.id, p.id, 1
FROM bundles b
JOIN products p ON p.campsite_id = b.campsite_id AND p.name = 'Riverside Pitch A'
WHERE b.name = 'Couple Escape Pack'
  AND NOT EXISTS (
    SELECT 1 FROM bundle_components bc WHERE bc.bundle_id = b.id AND bc.product_id = p.id
  );

INSERT INTO bundle_components (bundle_id, product_id, quantity)
SELECT b.id, p.id, 1
FROM bundles b
JOIN products p ON p.campsite_id = b.campsite_id AND p.name = 'BBQ Kit'
WHERE b.name = 'Couple Escape Pack'
  AND NOT EXISTS (
    SELECT 1 FROM bundle_components bc WHERE bc.bundle_id = b.id AND bc.product_id = p.id
  );

INSERT INTO bundle_components (bundle_id, product_id, quantity)
SELECT b.id, p.id, 1
FROM bundles b
JOIN products p ON p.campsite_id = b.campsite_id AND p.name = 'Firewood Bundle'
WHERE b.name = 'Couple Escape Pack'
  AND NOT EXISTS (
    SELECT 1 FROM bundle_components bc WHERE bc.bundle_id = b.id AND bc.product_id = p.id
  );
