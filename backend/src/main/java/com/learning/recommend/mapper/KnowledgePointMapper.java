package com.learning.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learning.recommend.entity.KnowledgePoint;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识点Mapper接口
 */
@Mapper
public interface KnowledgePointMapper extends BaseMapper<KnowledgePoint> {
}
