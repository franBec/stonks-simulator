INSERT INTO portfolio(cash_balance) SELECT 10000.00 WHERE NOT EXISTS (SELECT 1 FROM portfolio);
