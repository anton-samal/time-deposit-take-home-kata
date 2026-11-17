-- Demo data for running the application locally (not used by the tests).
-- Covers every plan type on both sides of its interest boundaries.
INSERT INTO time_deposits (id, plan_type, days, balance)
VALUES (1, 'basic', 30, 10000.00),     -- no interest yet (first 30 days)
       (2, 'basic', 31, 10000.00),     -- 1% / 12
       (3, 'student', 365, 20000.00),  -- 3% / 12, last day of the first year
       (4, 'student', 366, 20000.00),  -- no interest after one year
       (5, 'premium', 45, 50000.00),   -- no interest yet (first 45 days)
       (6, 'premium', 46, 50000.00);   -- 5% / 12

INSERT INTO withdrawals (id, time_deposit_id, amount, date)
VALUES (1, 2, 500.00, '2026-01-15'),
       (2, 2, 250.00, '2026-02-15'),
       (3, 6, 1000.00, '2026-03-01');
