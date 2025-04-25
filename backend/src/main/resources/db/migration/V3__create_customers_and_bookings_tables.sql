-- Create customers table
CREATE TABLE IF NOT EXISTS customers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create bookings table with foreign key to customers
CREATE TABLE IF NOT EXISTS bookings (
    id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    booking_date TIMESTAMP NOT NULL,
    amount DECIMAL(10, 2) NOT NULL
);

-- Create index on customer_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_bookings_customer_id ON bookings(customer_id);