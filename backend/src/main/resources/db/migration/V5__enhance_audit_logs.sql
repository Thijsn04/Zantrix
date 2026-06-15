-- Enhance Audit Logs with Cryptographic Hashing, IP, and Patient context
ALTER TABLE audit_logs 
    ADD COLUMN previous_hash VARCHAR(255),
    ADD COLUMN hash VARCHAR(255),
    ADD COLUMN ip_address VARCHAR(45),
    ADD COLUMN patient_id VARCHAR(50);

-- Table for Break The Glass temporary sessions
CREATE TABLE break_the_glass_sessions (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    reason TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);
