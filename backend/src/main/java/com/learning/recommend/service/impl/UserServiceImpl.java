package com.learning.recommend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.recommend.common.Constants;
import com.learning.recommend.dto.LoginDTO;
import com.learning.recommend.dto.RegisterDTO;
import com.learning.recommend.entity.User;
import com.learning.recommend.entity.UserProfile;
import com.learning.recommend.entity.UserBehavior;
import com.learning.recommend.entity.LearningMaterial;
import com.learning.recommend.entity.KnowledgePoint;
import com.learning.recommend.mapper.UserMapper;
import com.learning.recommend.mapper.UserProfileMapper;
import com.learning.recommend.mapper.UserBehaviorMapper;
import com.learning.recommend.mapper.LearningMaterialMapper;
import com.learning.recommend.mapper.KnowledgePointMapper;
import com.learning.recommend.service.UserService;
import com.learning.recommend.utils.JwtUtil;
import com.learning.recommend.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private UserBehaviorMapper behaviorMapper;

    @Autowired
    private LearningMaterialMapper materialMapper;

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setEmail(registerDTO.getEmail());
        user.setRole(Constants.ROLE_STUDENT);
        user.setStatus(Constants.STATUS_ENABLED);
        user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + registerDTO.getUsername());
        userMapper.insert(user);

        // 创建用户画像(冷启动)
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setLearningLevel(Constants.LEVEL_BEGINNER);
        profile.setLearningDuration(0);
        
        // 设置兴趣标签并初始化偏好
        if (registerDTO.getInterestTags() != null && !registerDTO.getInterestTags().isEmpty()) {
            profile.setInterestTags(JSONUtil.toJsonStr(registerDTO.getInterestTags()));
            // 默认将第一个兴趣标签作为初始偏好分类
            String firstTag = registerDTO.getInterestTags().get(0);
            profile.setFavoriteCategory(firstTag);
            Map<String, Double> prefs = new HashMap<>();
            prefs.put(firstTag, 1.0);
            profile.setKnowledgePreferences(JSONUtil.toJsonStr(prefs));
        } else {
            profile.setInterestTags("[]");
            profile.setKnowledgePreferences("{}");
        }
        
        profile.setLastActiveTime(LocalDateTime.now());
        userProfileMapper.insert(profile);
    }

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 检查状态
        if (user.getStatus().equals(Constants.STATUS_DISABLED)) {
            throw new RuntimeException("账号已被禁用");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", getUserInfo(user.getId()));
        return result;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);

        // 获取用户画像
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(wrapper);
        
        if (profile != null) {
            userVO.setLearningLevel(profile.getLearningLevel());
            
            // 将 JSON 字符串转换为 List<String>
            if (profile.getInterestTags() != null && !profile.getInterestTags().isEmpty()) {
                userVO.setInterestTags(JSONUtil.toList(profile.getInterestTags(), String.class));
            }
            
            userVO.setLearningDuration(profile.getLearningDuration());
            userVO.setFavoriteCategory(profile.getFavoriteCategory());
        }

        return userVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, UserVO userVO) {
        User user = new User();
        user.setId(userId);
        user.setNickname(userVO.getNickname());
        user.setEmail(userVO.getEmail());
        user.setAvatar(userVO.getAvatar());
        userMapper.updateById(user);

        // 更新画像
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(wrapper);
        
        if (profile != null && userVO.getInterestTags() != null) {
            // 将 List<String> 转换为 JSON 字符串
            String tagsJson = JSONUtil.toJsonStr(userVO.getInterestTags());
            profile.setInterestTags(tagsJson);

            // 如果用户选择了兴趣标签，则同步更新偏好分类
            if (!userVO.getInterestTags().isEmpty()) {
                String firstTag = userVO.getInterestTags().get(0);
                profile.setFavoriteCategory(firstTag);

                if (profile.getKnowledgePreferences() == null || profile.getKnowledgePreferences().isEmpty()) {
                    Map<String, Double> prefs = new HashMap<>();
                    prefs.put(firstTag, 1.0);
                    profile.setKnowledgePreferences(JSONUtil.toJsonStr(prefs));
                }
            }
            userProfileMapper.updateById(profile);
        }
    }

    // ========== 管理员功能实现 ==========

    @Override
    public Page<UserVO> getAdminUserList(String keyword, Integer role, Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        
        // 角色筛选
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        Page<User> userPage = userMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream()
                .map(user -> BeanUtil.copyProperties(user, UserVO.class))
                .collect(java.util.stream.Collectors.toList()));
        
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 校验状态是否相同
        if (user.getStatus().equals(status)) {
            String statusText = status == 1 ? "正常" : "禁用";
            throw new RuntimeException("该用户已经是" + statusText + "状态");
        }
        
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 不允许删除管理员
        if (user.getRole().equals(Constants.ROLE_ADMIN)) {
            throw new RuntimeException("不能删除管理员账号");
        }
        
        userMapper.deleteById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfoByAdmin(Long userId, UserVO userVO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 管理员可以更新更多字段
        if (userVO.getNickname() != null) {
            user.setNickname(userVO.getNickname());
        }
        if (userVO.getEmail() != null) {
            user.setEmail(userVO.getEmail());
        }
        if (userVO.getRole() != null) {
            user.setRole(userVO.getRole());
        }
        if (userVO.getStatus() != null) {
            user.setStatus(userVO.getStatus());
        }
        userMapper.updateById(user);

        // 更新画像
        if (userVO.getInterestTags() != null) {
            LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserProfile::getUserId, userId);
            UserProfile profile = userProfileMapper.selectOne(wrapper);
            
            if (profile != null) {
                String tagsJson = JSONUtil.toJsonStr(userVO.getInterestTags());
                profile.setInterestTags(tagsJson);
                userProfileMapper.updateById(profile);
            }
        }
    }

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 浏览次数
        LambdaQueryWrapper<UserBehavior> viewWrapper = new LambdaQueryWrapper<>();
        viewWrapper.eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getBehaviorType, Constants.BEHAVIOR_VIEW);
        long viewCount = behaviorMapper.selectCount(viewWrapper);
        stats.put("viewCount", viewCount);
        
        // 收藏次数
        LambdaQueryWrapper<UserBehavior> collectWrapper = new LambdaQueryWrapper<>();
        collectWrapper.eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getBehaviorType, Constants.BEHAVIOR_COLLECT);
        long collectCount = behaviorMapper.selectCount(collectWrapper);
        stats.put("collectCount", collectCount);
        
        // 评分次数
        LambdaQueryWrapper<UserBehavior> ratingWrapper = new LambdaQueryWrapper<>();
        ratingWrapper.eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getBehaviorType, Constants.BEHAVIOR_RATE);
        long ratingCount = behaviorMapper.selectCount(ratingWrapper);
        stats.put("ratingCount", ratingCount);
        
        // 学习时长(从用户画像获取)
        LambdaQueryWrapper<UserProfile> profileWrapper = new LambdaQueryWrapper<>();
        profileWrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(profileWrapper);
        
        int learningDuration = 0;
        if (profile != null && profile.getLearningDuration() != null) {
            learningDuration = profile.getLearningDuration();
        }
        // 转换为小时，保留一位小数
        double learningHours = Math.round(learningDuration / 3600.0 * 10) / 10.0;
        stats.put("learningTime", learningHours);
        
        return stats;
    }

    @Override
    public java.util.List<Map<String, Object>> getRecentViews(Long userId, Integer limit) {
        // 查询最近浏览记录
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getBehaviorType, Constants.BEHAVIOR_VIEW)
                .isNotNull(UserBehavior::getMaterialId)
                .orderByDesc(UserBehavior::getCreateTime)
                .last("LIMIT " + limit);
        
        java.util.List<UserBehavior> behaviors = behaviorMapper.selectList(wrapper);
        
        // 转换为结果
        return behaviors.stream().map(behavior -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", behavior.getId());
            item.put("materialId", behavior.getMaterialId());
            item.put("createTime", behavior.getCreateTime());
            item.put("duration", behavior.getDuration());
            
            // 获取资料信息
            LearningMaterial material = materialMapper.selectById(behavior.getMaterialId());
            if (material != null) {
                item.put("title", material.getTitle());
                item.put("category", material.getCategory());
            }
            
            return item;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.List<Map<String, Object>> getRecentKnowledgeViews(Long userId, Integer limit) {
        // 查询知识点最近浏览记录
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getBehaviorType, Constants.BEHAVIOR_VIEW)
                .isNotNull(UserBehavior::getKnowledgePointId)
                .orderByDesc(UserBehavior::getCreateTime)
                .last("LIMIT " + limit);
        
        java.util.List<UserBehavior> behaviors = behaviorMapper.selectList(wrapper);
        
        // 转换为结果
        return behaviors.stream().map(behavior -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", behavior.getId());
            item.put("knowledgePointId", behavior.getKnowledgePointId());
            item.put("createTime", behavior.getCreateTime());
            item.put("duration", behavior.getDuration());
            
            // 获取知识点信息
            com.learning.recommend.entity.KnowledgePoint kp = knowledgePointMapper.selectById(behavior.getKnowledgePointId());
            if (kp != null) {
                item.put("title", kp.getTitle());
                item.put("category", kp.getCategory());
            }
            
            return item;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean isAdminUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            // 假设管理员角色ID为Constants.ROLE_ADMIN
            // 在实际项目中，这里的判断逻辑可能会有所不同
            return user.getRole() != null && user.getRole().equals(com.learning.recommend.common.Constants.ROLE_ADMIN);
        }
        return false;
    }
}
