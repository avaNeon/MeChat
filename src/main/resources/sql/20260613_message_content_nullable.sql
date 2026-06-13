ALTER TABLE message
    MODIFY COLUMN content TEXT NULL COMMENT '消息内容，图片消息可为空';
