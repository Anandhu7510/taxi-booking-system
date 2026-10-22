-- Run this script once in MySQL 8.0+ before starting the Java desktop application.
CREATE DATABASE IF NOT EXISTS taxi_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE taxi_booking;

CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'CUSTOMER'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS drivers (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    license_no VARCHAR(80) NULL UNIQUE,
    vehicle_number VARCHAR(40) NOT NULL UNIQUE,
    vehicle_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Available',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_driver_status CHECK (status IN ('Available', 'On Trip', 'Offline'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reservations (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NULL,
    driver_id INT NOT NULL,
    customer_name VARCHAR(150) NOT NULL,
    customer_phone VARCHAR(30) NOT NULL,
    pickup_location VARCHAR(255) NOT NULL,
    drop_location VARCHAR(255) NOT NULL,
    scheduled_at DATETIME NOT NULL,
    service_type VARCHAR(40) NOT NULL,
    cab_type VARCHAR(30) NOT NULL,
    customer_vehicle VARCHAR(150) NOT NULL DEFAULT '',
    estimated_km DECIMAL(10,2) NOT NULL,
    fare DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    CONSTRAINT fk_reservation_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_reservation_driver FOREIGN KEY (driver_id) REFERENCES drivers(id),
    CONSTRAINT chk_reservation_status CHECK (status IN ('Pending','Ongoing','Completed','Cancelled')),
    CONSTRAINT chk_reservation_fare CHECK (fare >= 0),
    CONSTRAINT chk_reservation_km CHECK (estimated_km > 0),
    INDEX idx_customer (customer_id),
    INDEX idx_driver_status (driver_id, status)
) ENGINE=InnoDB;
