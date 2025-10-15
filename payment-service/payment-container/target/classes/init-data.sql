-- テスト用初期データ1セット目
-- credit_entryのデータ1件、credit_historyのデータ3件を追加
-- credit_historyのうち2件はCREDIT、1件はDEBIT
-- cutomer_idは全て同じUUIDを使用

-- Customer 1 の Credit Entry (残高: 500.00 = 100.00 + 600.00 - 200.00)
INSERT INTO payment.credit_entry(id, customer_id, total_credit_amount)
    VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb45', 'd215b5f8-0249-4dc5-89a3-51fd148cfb41', 500.00);

-- Customer 1 の Credit History 1回目 (入金 100.00)
INSERT INTO payment.credit_history(id, customer_id, amount, type)
    VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb47', 'd215b5f8-0249-4dc5-89a3-51fd148cfb41', 100.00, 'CREDIT');

-- Customer 1 の Credit History 2回目 (入金 600.00)
INSERT INTO payment.credit_history(id, customer_id, amount, type)
    VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb48', 'd215b5f8-0249-4dc5-89a3-51fd148cfb41', 600.00, 'CREDIT');

-- Customer 1 の Credit History 3回目 (支払い 200.00)
INSERT INTO payment.credit_history(id, customer_id, amount, type)
    VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb49', 'd215b5f8-0249-4dc5-89a3-51fd148cfb41', 200.00, 'DEBIT');


-- テスト用初期データ2セット目
-- credit_entryのデータ1件、credit_historyのデータ1件を追加
-- credit_historyのうち1件はCREDIT
-- cutomer_idは全て同じUUIDを使用(1セット目とは異なるUUID)

-- Customer 2 の Credit Entry (残高: 100.00)
INSERT INTO payment.credit_entry(id, customer_id, total_credit_amount)
    VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb46', 'd215b5f8-0249-4dc5-89a3-51fd148cfb42', 100.00);

-- Customer 2 の Credit History 1回目 (入金 100.00)
INSERT INTO payment.credit_history(id, customer_id, amount, type)
    VALUES ('d215b5f8-0249-4dc5-89a3-51fd148cfb50', 'd215b5f8-0249-4dc5-89a3-51fd148cfb42', 100.00, 'CREDIT');