DELETE FROM trade_history;
DELETE FROM position;
DELETE FROM portfolio;
INSERT INTO portfolio(id, cash_balance) VALUES (1, 8500.00);
INSERT INTO position(id, portfolio_id, symbol, quantity) VALUES (1, 1, 'GMEE', 10);
INSERT INTO position(id, portfolio_id, symbol, quantity) VALUES (2, 1, 'DOGE', 50);
