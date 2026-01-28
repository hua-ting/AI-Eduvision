-- ============================================
-- 学习推荐系统数据库初始化脚本
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS learning_recommend DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE learning_recommend;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT 'https://api.dicebear.com/7.x/avataaars/svg?seed=default' COMMENT '头像URL',
    email VARCHAR(100) COMMENT '邮箱',
    role TINYINT DEFAULT 0 COMMENT '角色:0学生 1管理员',
    status TINYINT DEFAULT 1 COMMENT '状态:0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username(username),
    INDEX idx_role(role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 用户画像表
-- ============================================
DROP TABLE IF EXISTS t_user_profile;
CREATE TABLE t_user_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户ID',
    learning_level VARCHAR(20) DEFAULT '初级' COMMENT '学习水平:初级/中级/高级',
    interest_tags JSON COMMENT '兴趣标签数组 ["算法","数据库"]',
    active_time JSON COMMENT '活跃时段统计',
    learning_duration INT DEFAULT 0 COMMENT '累计学习时长(分钟)',
    favorite_category VARCHAR(50) COMMENT '最喜欢的分类',
    profile_vector TEXT COMMENT '用户画像向量(JSON格式)',
    qa_topics JSON COMMENT '问答主题分布 {"algorithm": 5}',
    knowledge_preferences JSON COMMENT '知识点分类偏好 {"algorithm": 0.8}',
    total_qa_count INT DEFAULT 0 COMMENT '总问答次数',
    total_knowledge_views INT DEFAULT 0 COMMENT '知识点浏览次数',
    total_knowledge_collects INT DEFAULT 0 COMMENT '知识点收藏次数',
    last_active_time DATETIME COMMENT '最后活跃时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    INDEX idx_user_id(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户画像表';

-- ============================================
-- 3. 知识点表
-- ============================================
DROP TABLE IF EXISTS t_knowledge_point;
CREATE TABLE t_knowledge_point (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '知识点ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    category VARCHAR(50) NOT NULL COMMENT '分类:算法/数据库/AI等',
    sub_category VARCHAR(50) COMMENT '子分类',
    description TEXT COMMENT '简要描述',
    content TEXT COMMENT '知识点详细内容',
    tags JSON COMMENT '标签数组',
    difficulty VARCHAR(20) DEFAULT '中级' COMMENT '难度:初级/中级/高级',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    collect_count INT DEFAULT 0 COMMENT '收藏量',
    avg_rating DECIMAL(3,2) DEFAULT 0.00 COMMENT '平均评分(0-5)',
    rating_count INT DEFAULT 0 COMMENT '评分人数',
    status TINYINT DEFAULT 2 COMMENT '状态:0下架 1上架 2待审核',
    creator_id BIGINT COMMENT '创建者ID',
    related_points JSON COMMENT '相关知识点ID数组',
    prerequisites JSON COMMENT '前置知识点ID数组',
    audit_reason VARCHAR(500) COMMENT '审核原因/拒绝理由',
    audit_status TINYINT DEFAULT 2 COMMENT '审核状态:0待审核 1通过 2拒绝',
    pending_content TEXT COMMENT '待审核的内容',
    updated_by BIGINT COMMENT '更新者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category(category),
    INDEX idx_sub_category(sub_category),
    INDEX idx_status(status),
    INDEX idx_creator_id(creator_id),
    INDEX idx_create_time(create_time),
    INDEX idx_view_count(view_count),
    INDEX idx_avg_rating(avg_rating),
    INDEX idx_category_status(category, status),
    INDEX idx_difficulty_status(difficulty, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识点表';

-- ============================================
-- 4. 学习资料表（保留用于兼容）
-- ============================================
DROP TABLE IF EXISTS t_material;
CREATE TABLE t_material (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资料ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    category VARCHAR(50) NOT NULL COMMENT '分类:教材/课件/论文/其他',
    sub_category VARCHAR(50) COMMENT '子分类:算法/数据库/AI等',
    description TEXT COMMENT '描述',
    cover_url VARCHAR(500) COMMENT '封面图URL',
    file_url VARCHAR(500) COMMENT '文件URL',
    file_type VARCHAR(20) COMMENT '文件类型:pdf/docx/txt/pptx',
    file_size BIGINT COMMENT '文件大小(字节)',
    tags JSON COMMENT '标签数组',
    difficulty VARCHAR(20) DEFAULT '中级' COMMENT '难度:初级/中级/高级',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    collect_count INT DEFAULT 0 COMMENT '收藏量',
    download_count INT DEFAULT 0 COMMENT '下载量',
    avg_rating DECIMAL(3,2) DEFAULT 0.00 COMMENT '平均评分(0-5)',
    rating_count INT DEFAULT 0 COMMENT '评分人数',
    status TINYINT DEFAULT 1 COMMENT '状态:0下架 1上架 2待审核',
    uploader_id BIGINT COMMENT '上传者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category(category),
    INDEX idx_sub_category(sub_category),
    INDEX idx_status(status),
    INDEX idx_create_time(create_time),
    INDEX idx_view_count(view_count),
    INDEX idx_avg_rating(avg_rating),
    INDEX idx_category_status(category, status),
    INDEX idx_difficulty_status(difficulty, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习资料表';

-- ============================================
-- 5. 资料摘要表
-- ============================================
DROP TABLE IF EXISTS t_material_summary;
CREATE TABLE t_material_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    material_id BIGINT UNIQUE NOT NULL COMMENT '资料ID',
    summary_text TEXT COMMENT 'AI生成摘要',
    summary_type VARCHAR(20) DEFAULT 'extractive' COMMENT '摘要类型:extractive抽取式/abstractive生成式',
    key_points JSON COMMENT '关键要点数组',
    keywords JSON COMMENT '关键词数组',
    model_name VARCHAR(50) DEFAULT 'T5-Pegasus' COMMENT '使用模型:BART/T5-Pegasus/TextRank',
    generate_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (material_id) REFERENCES t_material(id) ON DELETE CASCADE,
    INDEX idx_material_id(material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资料摘要表';

-- ============================================
-- 6. 用户行为表
-- ============================================
DROP TABLE IF EXISTS t_user_behavior;
CREATE TABLE t_user_behavior (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '行为ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    material_id BIGINT COMMENT '资料ID(如果是资料相关行为)',
    knowledge_point_id BIGINT COMMENT '知识点ID(如果是知识点相关行为)',
    behavior_type VARCHAR(20) NOT NULL COMMENT '行为类型:view/collect/download/rate/uncollect',
    duration INT DEFAULT 0 COMMENT '浏览时长(秒)',
    rating TINYINT COMMENT '评分1-5',
    device VARCHAR(50) COMMENT '设备类型',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id(user_id),
    INDEX idx_material_id(material_id),
    INDEX idx_knowledge_point_id(knowledge_point_id),
    INDEX idx_user_material(user_id, material_id),
    INDEX idx_user_kp(user_id, knowledge_point_id),
    INDEX idx_behavior_type(behavior_type),
    INDEX idx_create_time(create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表';

-- ============================================
-- 7. 推荐记录表
-- ============================================
DROP TABLE IF EXISTS t_recommendation_log;
CREATE TABLE t_recommendation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    material_id BIGINT NOT NULL COMMENT '资料ID',
    score DECIMAL(5,4) COMMENT '推荐分数',
    algorithm VARCHAR(50) COMMENT '算法类型:CF/CB/Hybrid/Hot',
    reason VARCHAR(200) COMMENT '推荐理由',
    position INT COMMENT '推荐位置',
    is_clicked TINYINT DEFAULT 0 COMMENT '是否点击:0否 1是',
    is_collected TINYINT DEFAULT 0 COMMENT '是否收藏:0否 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id(user_id),
    INDEX idx_user_time(user_id, create_time),
    INDEX idx_is_clicked(is_clicked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐记录表';

-- ============================================
-- 8. 标签表
-- ============================================
DROP TABLE IF EXISTS t_tag;
CREATE TABLE t_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    name VARCHAR(50) UNIQUE NOT NULL COMMENT '标签名',
    category VARCHAR(50) COMMENT '分类',
    description VARCHAR(200) COMMENT '描述',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_category(category),
    INDEX idx_use_count(use_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- ============================================
-- 9. 用户收藏表
-- ============================================
DROP TABLE IF EXISTS t_user_collection;
CREATE TABLE t_user_collection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    material_id BIGINT COMMENT '资料ID',
    knowledge_point_id BIGINT COMMENT '知识点ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_material(user_id, material_id),
    UNIQUE KEY uk_user_kp(user_id, knowledge_point_id),
    INDEX idx_user_id(user_id),
    INDEX idx_material_id(material_id),
    INDEX idx_knowledge_point_id(knowledge_point_id),
    INDEX idx_create_time(create_time),
    INDEX idx_user_material_kp(user_id, material_id, knowledge_point_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- ============================================
-- 10. 问答记录表
-- ============================================
DROP TABLE IF EXISTS t_qa_record;
CREATE TABLE t_qa_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '问答ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    question TEXT NOT NULL COMMENT '用户问题',
    answer TEXT COMMENT 'AI回答',
    generated_kp_id BIGINT COMMENT '生成的知识点ID',
    model_name VARCHAR(50) DEFAULT 'qwen-turbo' COMMENT '使用的模型',
    duration INT DEFAULT 0 COMMENT '回答耗时(毫秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id(user_id),
    INDEX idx_generated_kp_id(generated_kp_id),
    INDEX idx_create_time(create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答记录表';

-- ============================================
-- 初始化管理员账号
-- ============================================
-- 密码: admin123 (BCrypt加密后)
INSERT INTO t_user (username, password, nickname, role, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lZdEJgEErgHM2fPCi', '系统管理员', 1, 1);

-- ============================================
-- 初始化常用标签
-- ============================================
INSERT INTO t_tag (name, category, description) VALUES
('算法', '计算机', '算法与数据结构相关'),
('数据库', '计算机', '数据库技术'),
('人工智能', '计算机', 'AI与机器学习'),
('前端开发', '计算机', 'Web前端技术'),
('后端开发', '计算机', '后端服务开发'),
('高等数学', '数学', '高等数学'),
('线性代数', '数学', '线性代数'),
('概率论', '数学', '概率论与数理统计'),
('大学英语', '语言', '英语学习'),
('计算机网络', '计算机', '网络技术');

-- ============================================
-- 插入知识点测试数据
-- ============================================
INSERT INTO t_knowledge_point (title, category, sub_category, description, content, difficulty, status, view_count, collect_count, avg_rating, creator_id) VALUES
('快速排序', '算法', '排序算法', '快速排序是一种高效的排序算法', '快速排序采用分治策略，通过选择基准元素将数组分为两部分，递归排序。平均时间复杂度O(nlogn)。', '中级', 1, 1, 1, 4.0, 1),
('MySQL索引', '数据库', 'MySQL', 'MySQL索引优化技术', 'MySQL索引是提高查询性能的关键，包括B+树索引、哈希索引等类型。合理使用索引可以大幅提升查询效率。', '高级', 1, 0, 0, 0.0, 1);

-- ============================================
-- 创建视图: 资料详情视图(含摘要)
-- ============================================
CREATE OR REPLACE VIEW v_material_detail AS
SELECT 
    m.*,
    s.summary_text,
    s.key_points,
    s.keywords,
    s.model_name,
    u.nickname AS uploader_name
FROM t_material m
LEFT JOIN t_material_summary s ON m.id = s.material_id
LEFT JOIN t_user u ON m.uploader_id = u.id;

-- ============================================
-- 创建存储过程: 更新资料统计
-- ============================================
DELIMITER //
CREATE PROCEDURE update_material_stats(IN p_material_id BIGINT)
BEGIN
    -- 更新浏览量
    UPDATE t_material 
    SET view_count = (
        SELECT COUNT(*) FROM t_user_behavior 
        WHERE material_id = p_material_id AND behavior_type = 'view'
    )
    WHERE id = p_material_id;
    
    -- 更新收藏量
    UPDATE t_material 
    SET collect_count = (
        SELECT COUNT(*) FROM t_user_collection 
        WHERE material_id = p_material_id
    )
    WHERE id = p_material_id;
    
    -- 更新平均评分
    UPDATE t_material 
    SET avg_rating = (
        SELECT IFNULL(AVG(rating), 0) FROM t_user_behavior 
        WHERE material_id = p_material_id AND behavior_type = 'rate' AND rating IS NOT NULL
    ),
    rating_count = (
        SELECT COUNT(*) FROM t_user_behavior 
        WHERE material_id = p_material_id AND behavior_type = 'rate' AND rating IS NOT NULL
    )
    WHERE id = p_material_id;
END //
DELIMITER ;

-- ============================================
-- 完成提示
-- ============================================
SELECT '数据库初始化完成!' AS message;
SELECT CONCAT('管理员账号: admin, 密码: admin123') AS admin_info;
