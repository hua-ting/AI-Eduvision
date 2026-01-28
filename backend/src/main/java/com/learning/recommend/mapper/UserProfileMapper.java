package com.learning.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learning.recommend.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户画像 Mapper
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
