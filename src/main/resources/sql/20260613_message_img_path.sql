ALTER TABLE message
    ADD COLUMN img_path VARCHAR(512) DEFAULT NULL COMMENT '聊天图片相对于storage目录的路径'
    AFTER content;
