CREATE TABLE IF NOT EXISTS friend_request
(
    id              BIGINT       NOT NULL COMMENT '好友申请ID',
    requester_id    BIGINT       NOT NULL COMMENT '申请发起人用户ID',
    addressee_id    BIGINT       NOT NULL COMMENT '申请接收人用户ID',
    user_low_id     BIGINT       NOT NULL COMMENT '较小的用户ID',
    user_high_id    BIGINT       NOT NULL COMMENT '较大的用户ID',
    request_message VARCHAR(255) NULL COMMENT '好友申请留言',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0待处理，1已同意，2已拒绝',
    handle_time     DATETIME     NULL COMMENT '处理时间',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_friend_request_users (user_low_id, user_high_id),
    KEY idx_friend_request_addressee_status (addressee_id, status),
    KEY idx_friend_request_requester (requester_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '好友申请表';
