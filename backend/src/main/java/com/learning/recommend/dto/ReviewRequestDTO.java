package com.learning.recommend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 审核请求DTO
 */
@Data
@ApiModel("审核请求DTO")
public class ReviewRequestDTO {
    
    @ApiModelProperty("内容ID")
    private Long contentId;
    
    @ApiModelProperty("内容")
    private String content;
    
    @ApiModelProperty("用户ID")
    private Long userId;
}