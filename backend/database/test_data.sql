-- ============================================
-- 测试数据生成脚本
-- ============================================

USE learning_recommend;

-- ============================================
-- 1. 插入测试学生用户 (20个)
-- 密码统一: 123456
-- ============================================
INSERT INTO t_user (username, password, nickname, email, role, status) VALUES
('student01', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '张三', 'zhangsan@example.com', 0, 1),
('student02', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '李四', 'lisi@example.com', 0, 1),
('student03', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '王五', 'wangwu@example.com', 0, 1),
('student04', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '赵六', 'zhaoliu@example.com', 0, 1),
('student05', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '孙七', 'sunqi@example.com', 0, 1),
('student06', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '周八', 'zhouba@example.com', 0, 1),
('student07', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '吴九', 'wujiu@example.com', 0, 1),
('student08', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '郑十', 'zhengshi@example.com', 0, 1),
('student09', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '陈一', 'chenyi@example.com', 0, 1),
('student10', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '刘二', 'liuer@example.com', 0, 1),
('student11', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '杨三', 'yangsan@example.com', 0, 1),
('student12', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '黄四', 'huangsi@example.com', 0, 1),
('student13', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '朱五', 'zhuwu@example.com', 0, 1),
('student14', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '林六', 'linliu@example.com', 0, 1),
('student15', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '徐七', 'xuqi@example.com', 0, 1),
('student16', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '胡八', 'huba@example.com', 0, 1),
('student17', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '高九', 'gaojiu@example.com', 0, 1),
('student18', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '梁十', 'liangshi@example.com', 0, 1),
('student19', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '何一', 'heyi@example.com', 0, 1),
('student20', '$2a$10$EbmR2LBGwY0H1xqPZvSUwO5xzjN.OcT9KTXb8QnE3h.0Qx5FYN0kW', '罗二', 'luoer@example.com', 0, 1);

-- ============================================
-- 2. 为用户创建画像 (兴趣标签各异)
-- ============================================
INSERT INTO t_user_profile (user_id, learning_level, interest_tags, learning_duration, favorite_category) VALUES
(2, '中级', '["算法", "数据库"]', 120, '教材'),
(3, '初级', '["前端开发", "大学英语"]', 60, '课件'),
(4, '高级', '["人工智能", "算法"]', 300, '论文'),
(5, '中级', '["后端开发", "数据库"]', 180, '教材'),
(6, '初级', '["高等数学", "线性代数"]', 90, '课件'),
(7, '中级', '["计算机网络", "数据库"]', 150, '教材'),
(8, '高级', '["人工智能", "算法", "概率论"]', 400, '论文'),
(9, '初级', '["前端开发"]', 50, '课件'),
(10, '中级', '["算法", "数据库", "后端开发"]', 200, '教材'),
(11, '初级', '["大学英语", "高等数学"]', 70, '课件'),
(12, '高级', '["人工智能", "深度学习"]', 350, '论文'),
(13, '中级', '["数据库", "后端开发"]', 160, '教材'),
(14, '初级', '["前端开发", "算法"]', 80, '课件'),
(15, '中级', '["计算机网络", "算法"]', 140, '教材'),
(16, '高级', '["人工智能"]', 320, '论文'),
(17, '初级', '["高等数学", "概率论"]', 60, '课件'),
(18, '中级', '["数据库", "算法"]', 175, '教材'),
(19, '初级', '["前端开发", "大学英语"]', 65, '课件'),
(20, '高级', '["算法", "人工智能"]', 380, '论文'),
(21, '中级', '["后端开发", "数据库"]', 190, '教材');

-- 注意:后续开发时会完善测试资料和行为数据
-- 此处暂时只初始化基础用户数据

SELECT '测试数据生成完成!' AS message;
SELECT CONCAT('测试学生账号: student01-student20, 密码统一: 123456') AS test_account;
