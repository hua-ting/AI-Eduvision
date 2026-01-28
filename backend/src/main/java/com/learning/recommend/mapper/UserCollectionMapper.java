package com.learning.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learning.recommend.entity.UserCollection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收藏Mapper
 */
@Mapper
public interface UserCollectionMapper extends BaseMapper<UserCollection> {
}
