-- Run this directly in MySQL Workbench or command line to see EXACTLY
-- what's stored for every product right now.
-- This will tell us immediately if there's a data problem vs a code problem.

USE electrasync_db;

SELECT
    id,
    sku,
    name,
    stock_quantity,
    minimum_stock,
    active,
    CASE
        WHEN stock_quantity <= minimum_stock THEN 'SHOULD show as Low Stock'
        ELSE 'Should NOT show as Low Stock'
    END AS expected_result
FROM products
ORDER BY id;
