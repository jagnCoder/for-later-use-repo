-- Create the kyc_db database
CREATE DATABASE IF NOT EXISTS kyc_db;

-- Use the newly created database
USE kyc_db;

-- Table to store user information
-- id is the primary key and auto-increments for a unique user ID
-- full_name stores the user's name
-- date_of_birth stores the user's date of birth in YYYY-MM-DD format
-- address stores the user's residential address
CREATE TABLE IF NOT EXISTS users (
id INT AUTO_INCREMENT PRIMARY KEY,
full_name VARCHAR(255) NOT NULL,
date_of_birth DATE,
address VARCHAR(255)
);

-- Table to store document information for each user
-- id is the primary key and auto-increments for a unique document ID
-- user_id is a foreign key that links this document to a specific user in the users table
-- document_type specifies the type of document (e.g., 'Aadhaar Card', 'Passport', 'Voter ID')
-- document_number stores the unique identifier of the document
-- verification_status can be 'Pending', 'Verified', or 'Rejected'
-- FOREIGN KEY (user_id) ensures data integrity by preventing orphaned documents
CREATE TABLE IF NOT EXISTS documents (
id INT AUTO_INCREMENT PRIMARY KEY,
user_id INT NOT NULL,
document_type VARCHAR(50) NOT NULL,
document_number VARCHAR(100) NOT NULL,
verification_status VARCHAR(20) DEFAULT 'Pending',
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);