INSERT INTO item (id, name, price, description)
VALUES (1, 'Test Item', 19999, 'Тестовый товар');

INSERT INTO cart (id, total_price)
VALUES (1, 19999);

INSERT INTO cart_items (cart_id, item_id, quantity)
VALUES (1, 1, 1);