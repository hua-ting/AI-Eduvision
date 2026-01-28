package com.learning.recommend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 内容更新DTO
 */
@Data
@ApiModel("内容更新DTO")
public class ContentUpdateDTO {
    
    @ApiModelProperty("内容ID")
    private Long id;
    
    @ApiModelProperty("内容")
    private String content;
}