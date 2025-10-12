insert into users (id, username, password, role)
VALUES (1, 'test_user', 'testpassword', 'ROLE_TEST');

INSERT INTO item (id, name, price, description)
VALUES (2, 'Test Item', 19999, 'Тестовый товар');

INSERT INTO cart (id, total_price, user_id)
VALUES (2, 19999, 1);

INSERT INTO cart_items (cart_id, item_id, quantity)
VALUES (2, 2, 1);
