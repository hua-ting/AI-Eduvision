package com.learning.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learning.recommend.entity.QARecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问答记录Mapper
 */
@Mapper
public interface QARecordMapper extends BaseMapper<QARecord> {
}
