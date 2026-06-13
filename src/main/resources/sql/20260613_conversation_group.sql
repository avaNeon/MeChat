ALTER TABLE conversation
    ADD COLUMN type     TINYINT  NOT NULL DEFAULT 0 COMMENT '会话类型：0 单聊，1 群聊' AFTER id,
    ADD COLUMN group_id BIGINT   NULL     COMMENT '群聊ID，群聊会话时使用' AFTER user_high_id,
    MODIFY COLUMN user_low_id BIGINT NULL,
    MODIFY COLUMN user_high_id BIGINT NULL;

ALTER TABLE message
    ADD COLUMN group_id BIGINT NULL COMMENT '群聊ID，群聊消息时使用' AFTER conversation_id;

ALTER TABLE group_chat_member
    ADD COLUMN last_read_message_id BIGINT NOT NULL DEFAULT 0 COMMENT '最后已读消息ID' AFTER role;
