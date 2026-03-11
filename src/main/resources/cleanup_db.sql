-- ============================================================
-- RevConnect DB Cleanup Script
-- Run this in MySQL Workbench or CLI, then restart the Spring Boot app.
-- Hibernate (ddl-auto=update) will recreate all tables correctly.
-- ============================================================

USE revconnectapp_db;

-- Disable FK checks so we can drop in any order
SET FOREIGN_KEY_CHECKS = 0;


DROP TABLE IF EXISTS follows;
DROP TABLE IF EXISTS post_likes;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS connections;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS blocks;
DROP TABLE IF EXISTS post_tagged_products;


-- Re-enable FK checks
SET FOREIGN_KEY_CHECKS = 1;

-- Done! Now restart the Spring Boot application.
-- Hibernate will recreate all tables with the correct schema,
-- and DataInitializer will seed fresh demo data.