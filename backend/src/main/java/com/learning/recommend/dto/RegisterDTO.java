package com.learning.recommend.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 注册请求DTO
 */
@Data
public class RegisterDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度4-20个字符")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20个字符")
    private String password;
    
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    // 兴趣标签(用于冷启动)
    private List<String> interestTags;
}
