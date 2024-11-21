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

-- Create the Access Control List table if it does not already exist
CREATE TABLE IF NOT EXISTS ACL (
    Role VARCHAR(255) PRIMARY KEY,
    Permissions VARCHAR(255) NOT NULL
);

