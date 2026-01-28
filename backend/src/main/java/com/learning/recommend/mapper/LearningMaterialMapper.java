package com.learning.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learning.recommend.entity.LearningMaterial;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习资料Mapper
 */
@Mapper
public interface LearningMaterialMapper extends BaseMapper<LearningMaterial> {
}
