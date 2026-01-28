package com.learning.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learning.recommend.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
