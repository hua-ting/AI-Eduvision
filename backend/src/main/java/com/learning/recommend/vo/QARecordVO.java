package com.learning.recommend.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 问答记录VO
 */
@Data
public class QARecordVO {
    private Long id;
    private String question;
    private String answer;
    private Long generatedKpId;
    private String generatedKpTitle;  // 关联的知识点标题
    private String modelName;
    private Integer duration;
    private LocalDateTime createTime;
}
