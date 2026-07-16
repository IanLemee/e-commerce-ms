CREATE TABLE users_tb (
    user_id SERIAL PRIMARY KEY,
    user_uuid UUID NOT NULL,
    user_name VARCHAR(64) NOT NULL,
    user_email VARCHAR(255) UNIQUE NOT NULL,
    user_password VARCHAR(255) NOT NULL,
    user_profile_pic VARCHAR(255),
    user_role VARCHAR(50) NOT NULL,
    user_acct_active BOOLEAN NOT NULL,
    user_verification_code INT NOT NULL
);