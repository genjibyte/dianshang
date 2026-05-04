-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    version INT NOT NULL DEFAULT 0,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 店铺表
CREATE TABLE IF NOT EXISTS t_shop (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(256) NOT NULL,
    phone VARCHAR(20) NOT NULL DEFAULT '',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-营业中 0-休息中',
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 商品表
CREATE TABLE IF NOT EXISTS t_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-上架 0-下架',
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 订单表
CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    shop_id BIGINT NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-待支付 1-已支付 2-配送中 3-已完成 4-已取消',
    delivery_address VARCHAR(256) NOT NULL,
    remark VARCHAR(256) DEFAULT '',
    cancel_reason VARCHAR(256) DEFAULT '',
    paid_time TIMESTAMP NULL,
    delivered_time TIMESTAMP NULL,
    completed_time TIMESTAMP NULL,
    cancelled_time TIMESTAMP NULL,
    version INT NOT NULL DEFAULT 0,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_order_no UNIQUE (order_no)
);

-- 订单明细表
CREATE TABLE IF NOT EXISTS t_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 支付表
CREATE TABLE IF NOT EXISTS t_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_no VARCHAR(64) NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    pay_method TINYINT NOT NULL COMMENT '1-余额 2-微信 3-支付宝',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-待支付 1-成功 2-失败 3-已退款',
    transaction_id VARCHAR(128) DEFAULT '' COMMENT '第三方流水号',
    paid_time TIMESTAMP NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_payment_no UNIQUE (payment_no)
);

-- 幂等表
CREATE TABLE IF NOT EXISTS t_idempotency (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL,
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型: CREATE_ORDER / PAY_ORDER',
    biz_id VARCHAR(64) NOT NULL COMMENT '业务结果ID（如 orderNo）',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_idempotency UNIQUE (idempotency_key, biz_type)
);

CREATE INDEX IF NOT EXISTS idx_order_user ON t_order(user_id);
CREATE INDEX IF NOT EXISTS idx_order_shop ON t_order(shop_id);
CREATE INDEX IF NOT EXISTS idx_order_item_order_no ON t_order_item(order_no);
CREATE INDEX IF NOT EXISTS idx_payment_order_no ON t_payment(order_no);
