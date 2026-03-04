CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    description VARCHAR(1000),
    image_url VARCHAR(500)
);

CREATE TABLE payment_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(27) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    amount INT NOT NULL,
    access_id VARCHAR(255),
    access_pass VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    tran_id VARCHAR(255),
    approve VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(id)
);
