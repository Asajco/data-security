-- Check if the database 'authdatabase' exists, and create it if it doesn't
CREATE DATABASE IF NOT EXISTS authdatabase;

-- Use the 'authdatabase'
USE authdatabase;

-- Create the Users table if it does not already exist
CREATE TABLE IF NOT EXISTS Users (
    Username VARCHAR(255) PRIMARY KEY,
    Password VARCHAR(255) NOT NULL,
    Role VARCHAR(255)
    );

-- Insert initial data into the Users table
-- If the username already exists, update the Password and Role fields
INSERT INTO Users (Username, Password, Role)
VALUES ('user', '1234', 'USER'),
       ('admin', '1234', 'ADMIN')
ON DUPLICATE KEY UPDATE
    Password = VALUES(Password),
    Role = VALUES(Role);