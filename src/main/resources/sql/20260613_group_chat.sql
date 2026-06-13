CREATE TABLE IF NOT EXISTS group_chat
(
    id          BIGINT       NOT NULL COMMENT '群聊ID',
    group_name  VARCHAR(255) NOT NULL COMMENT '群名称',
    owner_id    BIGINT       NOT NULL COMMENT '群主用户ID',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '群头像',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_group_chat_owner (owner_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '群聊表';

CREATE TABLE IF NOT EXISTS group_chat_member
(
    id        BIGINT   NOT NULL COMMENT '成员关系ID',
    group_id  BIGINT   NOT NULL COMMENT '群聊ID',
    user_id   BIGINT   NOT NULL COMMENT '用户ID',
    role      TINYINT  NOT NULL DEFAULT 0 COMMENT '角色：0 普通成员，1 群主',
    join_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_member (group_id, user_id),
    KEY idx_group_member_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '群聊成员表';
