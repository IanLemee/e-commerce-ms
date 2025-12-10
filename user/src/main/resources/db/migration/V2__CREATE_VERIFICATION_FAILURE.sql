CREATE TABLE verification_failure_tb(
    failed_id SERIAL PRIMARY KEY,
    failed_user_email VARCHAR(255),
    failed_user_code INT
);