package com.learning.recommend.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息VO
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private Integer role;
    private Integer status;
    private LocalDateTime createTime;
    
    // 用户画像信息
    private String learningLevel;
    private List<String> interestTags;  // 修改为 List<String>
    private Integer learningDuration;
    private String favoriteCategory;
}
