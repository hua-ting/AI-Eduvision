-- 添加缺失的字段
ALTER TABLE t_knowledge_point
    ADD COLUMN category VARCHAR(50) NOT NULL COMMENT '分类:算法/数据库/AI等' AFTER title,
    ADD COLUMN sub_category VARCHAR(50) COMMENT '子分类' AFTER category,
    ADD COLUMN description TEXT COMMENT '简要描述' AFTER sub_category,
    ADD COLUMN content TEXT COMMENT '知识点详细内容' AFTER description,
    ADD COLUMN difficulty VARCHAR(20) DEFAULT '中级' COMMENT '难度:初级/中级/高级' AFTER tags,
    ADD COLUMN view_count INT DEFAULT 0 COMMENT '浏览量' AFTER difficulty,
    ADD COLUMN collect_count INT DEFAULT 0 COMMENT '收藏量' AFTER view_count,
    ADD COLUMN avg_rating DECIMAL(3,2) DEFAULT 0.00 COMMENT '平均评分(0-5)' AFTER collect_count,
    ADD COLUMN rating_count INT DEFAULT 0 COMMENT '评分人数' AFTER avg_rating,
    ADD COLUMN status TINYINT DEFAULT 1 COMMENT '状态:0下架 1上架 2待审核' AFTER rating_count,
    ADD COLUMN creator_id BIGINT COMMENT '创建者ID' AFTER status,
    ADD COLUMN related_points JSON COMMENT '相关知识点ID数组' AFTER creator_id,
    ADD COLUMN prerequisites JSON COMMENT '前置知识点ID数组' AFTER related_points,
    ADD COLUMN audit_reason VARCHAR(500) COMMENT '审核原因/拒绝理由' AFTER prerequisites;

-- 添加索引
ALTER TABLE t_knowledge_point
    ADD INDEX idx_category(category),
    ADD INDEX idx_sub_category(sub_category),
    ADD INDEX idx_status(status),
    ADD INDEX idx_creator_id(creator_id),
    ADD INDEX idx_view_count(view_count),
    ADD INDEX idx_avg_rating(avg_rating);

-- 设置现有数据为上架状态
UPDATE t_knowledge_point SET status = 1 WHERE status IS NULL OR status = 0 OR status = 2;