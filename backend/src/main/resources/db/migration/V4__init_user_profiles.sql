CREATE TABLE user_profiles (
    id VARCHAR(255) PRIMARY KEY,
    theme VARCHAR(50) NOT NULL DEFAULT 'light',
    language VARCHAR(50) NOT NULL DEFAULT 'en'
);
