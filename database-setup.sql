-- ============================================================
-- ElectraSync ERP — MySQL Database Setup Script
-- Run this ONCE before starting the application
-- ============================================================

-- 1. Create the database
CREATE DATABASE IF NOT EXISTS electrasync_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE electrasync_db;

-- 2. (Optional) Create a dedicated MySQL user for the app
--    Recommended for production. Skip for local dev if using root.
-- CREATE USER 'electrasync_user'@'localhost' IDENTIFIED BY 'StrongPass@123';
-- GRANT ALL PRIVILEGES ON electrasync_db.* TO 'electrasync_user'@'localhost';
-- FLUSH PRIVILEGES;

-- NOTE: The application uses spring.jpa.hibernate.ddl-auto=update
-- so Hibernate will CREATE all tables automatically on first run.
-- You do NOT need to create tables manually.
-- This file only creates the database itself.
