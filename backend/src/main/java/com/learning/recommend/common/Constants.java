package com.learning.recommend.common;

/**
 * 常量类
 */
public class Constants {
    
    // 用户角色
    public static final Integer ROLE_STUDENT = 0;
    public static final Integer ROLE_ADMIN = 1;
    
    // 用户状态
    public static final Integer STATUS_DISABLED = 0;
    public static final Integer STATUS_ENABLED = 1;
    
    // 资料分类
    public static final String CATEGORY_TEXTBOOK = "教材";
    public static final String CATEGORY_COURSEWARE = "课件";
    public static final String CATEGORY_PAPER = "论文";
    public static final String CATEGORY_OTHER = "其他";
    
    // 资料状态
    public static final Integer MATERIAL_STATUS_OFFLINE = 0;
    public static final Integer MATERIAL_STATUS_ONLINE = 1;
    public static final Integer MATERIAL_STATUS_PENDING = 2;
    
    // 行为类型
    public static final String BEHAVIOR_VIEW = "view";
    public static final String BEHAVIOR_COLLECT = "collect";
    public static final String BEHAVIOR_DOWNLOAD = "download";
    public static final String BEHAVIOR_RATE = "rate";
    public static final String BEHAVIOR_UNCOLLECT = "uncollect";
    
    // 推荐算法类型
    public static final String ALGORITHM_CF = "CF";  // 协同过滤
    public static final String ALGORITHM_CB = "CB";  // 基于内容
    public static final String ALGORITHM_HYBRID = "Hybrid";  // 混合
    public static final String ALGORITHM_HOT = "Hot";  // 热门
    
    // Redis Key前缀
    public static final String REDIS_KEY_RECOMMEND = "recommend:user:";
    public static final String REDIS_KEY_HOT_MATERIALS = "hot:materials";
    public static final String REDIS_KEY_USER_PROFILE = "profile:user:";
    
    // 学习水平
    public static final String LEVEL_BEGINNER = "初级";
    public static final String LEVEL_INTERMEDIATE = "中级";
    public static final String LEVEL_ADVANCED = "高级";
    
    // 摘要类型
    public static final String SUMMARY_EXTRACTIVE = "extractive";  // 抽取式
    public static final String SUMMARY_ABSTRACTIVE = "abstractive";  // 生成式
}
