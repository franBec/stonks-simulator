DELETE FROM trade_history;
DELETE FROM position;
DELETE FROM portfolio;
INSERT INTO portfolio(id, cash_balance) VALUES (1, 10000.00);
INSERT INTO trade_history(id, portfolio_id, action, symbol, quantity, price, total_cost, cash_balance_after, executed_at)
VALUES (1, 1, 'BUY', 'GMEE', 10, 45.00, 450.00, 9550.00, '2026-01-03 17:00:00');
INSERT INTO trade_history(id, portfolio_id, action, symbol, quantity, price, total_cost, cash_balance_after, executed_at)
VALUES (2, 1, 'SELL', 'GMEE', 5, 50.00, 250.00, 9800.00, '2026-01-04 17:00:00');
