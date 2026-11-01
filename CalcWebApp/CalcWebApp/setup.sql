CREATE DATABASE IF NOT EXISTS calcdb;
USE calcdb;

CREATE TABLE IF NOT EXISTS calculation (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    operand1   DOUBLE       NOT NULL,
    operand2   DOUBLE       NULL,
    operation  VARCHAR(20)  NOT NULL,
    result     DOUBLE       NULL,
    error_msg  VARCHAR(100) NULL,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- to view saved calculations:
-- SELECT * FROM calcdb.calculation ORDER BY id DESC;
