-- Restaurant 1 のテストデータ
INSERT INTO restaurant.restaurants(id, name, active)
    VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb45', 'Restaurant 1', true);

-- Restaurant 1 の Product 1 (50.00円、利用可能)
INSERT INTO restaurant.restaurant_products(id, restaurant_id, product_id, product_name, price, available)
    VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb47', 'd215b5f8-0249-4dc5-89a3-51fd148cfb45', 
            'd215b5f8-0249-4dc5-89a3-51fd148cfb48', 'Product-1', 50.00, true);

-- Restaurant 1 の Product 2 (100.00円、利用可能)
INSERT INTO restaurant.restaurant_products(id, restaurant_id, product_id, product_name, price, available)
    VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb49', 'd215b5f8-0249-4dc5-89a3-51fd148cfb45', 
            'd215b5f8-0249-4dc5-89a3-51fd148cfb50', 'Product-2', 100.00, true);


-- Restaurant 2 のテストデータ
INSERT INTO restaurant.restaurants(id, name, active)
    VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb46', 'Restaurant 2', true);

-- Restaurant 2 の Product 3 (75.00円、利用可能)
INSERT INTO restaurant.restaurant_products(id, restaurant_id, product_id, product_name, price, available)
    VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb51', 'd215b5f8-0249-4dc5-89a3-51fd148cfb46', 
            'd215b5f8-0249-4dc5-89a3-51fd148cfb52', 'Product-3', 75.00, true);

-- Restaurant 2 の Product 4 (150.00円、利用不可)
INSERT INTO restaurant.restaurant_products(id, restaurant_id, product_id, product_name, price, available)
    VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb53', 'd215b5f8-0249-4dc5-89a3-51fd148cfb46', 
            'd215b5f8-0249-4dc5-89a3-51fd148cfb54', 'Product-4', 150.00, false);

-- マテリアライズドビューをリフレッシュ
REFRESH MATERIALIZED VIEW restaurant.order_restaurant_m_view;
