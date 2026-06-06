CREATE TABLE IF NOT EXISTS friend_relation
(
    id           BIGINT      NOT NULL COMMENT '好友关系ID',
    user_low_id  BIGINT      NOT NULL COMMENT '较小的用户ID',
    user_high_id BIGINT      NOT NULL COMMENT '较大的用户ID',
    initiator_id BIGINT      NOT NULL COMMENT '发起添加好友的用户ID',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_friend_relation_users (user_low_id, user_high_id),
    KEY idx_friend_relation_user_low (user_low_id),
    KEY idx_friend_relation_user_high (user_high_id),
    KEY idx_friend_relation_initiator (initiator_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '好友关系表';
