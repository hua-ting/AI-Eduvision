package com.learning.recommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.recommend.dto.KnowledgePointQueryDTO;
import com.learning.recommend.entity.KnowledgePoint;
import com.learning.recommend.entity.UserBehavior;
import com.learning.recommend.entity.UserCollection;
import com.learning.recommend.mapper.KnowledgePointMapper;
import com.learning.recommend.mapper.UserBehaviorMapper;
import com.learning.recommend.mapper.UserCollectionMapper;
import com.learning.recommend.service.KnowledgePointService;
import com.learning.recommend.service.UserProfileService;
import com.learning.recommend.vo.KnowledgePointVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * 知识点服务实现
 */
@Service
@RequiredArgsConstructor
public class KnowledgePointServiceImpl implements KnowledgePointService {

    private final KnowledgePointMapper knowledgePointMapper;
    private final UserBehaviorMapper userBehaviorMapper;
    private final UserCollectionMapper userCollectionMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired(required = false)
    private UserProfileService userProfileService; // 注入用户画像服务

    @Override
    public Page<KnowledgePointVO> getKnowledgePointList(KnowledgePointQueryDTO queryDTO, Long userId) {
        Page<KnowledgePoint> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<KnowledgePoint> wrapper = buildQueryWrapper(queryDTO);
        wrapper.eq(KnowledgePoint::getStatus, 1); // 只查上架的
        
        Page<KnowledgePoint> resultPage = knowledgePointMapper.selectPage(page, wrapper);
        return convertToVOPage(resultPage, userId);
    }

    @Override
    public KnowledgePointVO getKnowledgePointDetail(Long id, Long userId) {
        KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(id);
        if (knowledgePoint == null || knowledgePoint.getStatus() != 1) {
            return null;
        }
        return convertToVO(knowledgePoint, userId);
    }

    @Override
    public KnowledgePointVO getKnowledgePointDetail(Long id) {
        KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(id);
        if (knowledgePoint == null) {
            return null;
        }
        return convertToVO(knowledgePoint, null);
    }

    @Override
    @Transactional
    public void recordView(Long id, Long userId, Integer duration) {
        KnowledgePoint kp = knowledgePointMapper.selectById(id);
        
        // 记录浏览行为
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setKnowledgePointId(id);
        behavior.setBehaviorType("view");
        behavior.setDuration(duration);
        userBehaviorMapper.insert(behavior);
        
        // 更新浏览量
        knowledgePointMapper.update(null, 
            new LambdaUpdateWrapper<KnowledgePoint>()
                .setSql("view_count = view_count + 1")
                .eq(KnowledgePoint::getId, id)
        );
        
        // 异步更新用户画像
        if (userProfileService != null && kp != null) {
            userProfileService.recordKnowledgeView(userId, id, kp.getCategory(), duration);
        }
    }

    @Override
    @Transactional
    public void toggleCollect(Long id, Long userId) {
        LambdaQueryWrapper<UserCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollection::getUserId, userId)
               .eq(UserCollection::getKnowledgePointId, id);
        
        UserCollection existing = userCollectionMapper.selectOne(wrapper);
        
        if (existing != null) {
            // 取消收藏
            userCollectionMapper.deleteById(existing.getId());
            
            // 记录取消收藏行为
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setKnowledgePointId(id);
            behavior.setBehaviorType("uncollect");
            userBehaviorMapper.insert(behavior);
            
            // 更新收藏量
            knowledgePointMapper.update(null,
                new LambdaUpdateWrapper<KnowledgePoint>()
                    .setSql("collect_count = collect_count - 1")
                    .eq(KnowledgePoint::getId, id)
                    .gt(KnowledgePoint::getCollectCount, 0)
            );
        } else {
            // 收藏
            UserCollection collection = new UserCollection();
            collection.setUserId(userId);
            collection.setKnowledgePointId(id);
            userCollectionMapper.insert(collection);
            
            // 记录收藏行为
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setKnowledgePointId(id);
            behavior.setBehaviorType("collect");
            userBehaviorMapper.insert(behavior);
            
            // 更新收藏量
            knowledgePointMapper.update(null,
                new LambdaUpdateWrapper<KnowledgePoint>()
                    .setSql("collect_count = collect_count + 1")
                    .eq(KnowledgePoint::getId, id)
            );
            
            // 异步更新用户画像
            KnowledgePoint kp = knowledgePointMapper.selectById(id);
            if (userProfileService != null && kp != null) {
                userProfileService.recordKnowledgeCollect(userId, id, kp.getCategory());
            }
        }
    }

    @Override
    @Transactional
    public void rateKnowledgePoint(Long id, Long userId, Integer rating) {
        // 记录评分行为
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setKnowledgePointId(id);
        behavior.setBehaviorType("rate");
        behavior.setRating(rating);
        userBehaviorMapper.insert(behavior);
        
        // 重新计算平均评分
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehavior::getKnowledgePointId, id)
               .eq(UserBehavior::getBehaviorType, "rate")
               .isNotNull(UserBehavior::getRating);
        
        List<UserBehavior> ratings = userBehaviorMapper.selectList(wrapper);
        double avgRating = ratings.stream()
            .mapToInt(UserBehavior::getRating)
            .average()
            .orElse(0.0);
        
        knowledgePointMapper.update(null,
            new LambdaUpdateWrapper<KnowledgePoint>()
                .set(KnowledgePoint::getAvgRating, avgRating)
                .set(KnowledgePoint::getRatingCount, ratings.size())
                .eq(KnowledgePoint::getId, id)
        );
    }

    @Override
    public Page<KnowledgePointVO> getUserCollections(Long userId, Integer pageNum, Integer pageSize) {
        Page<UserCollection> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollection::getUserId, userId)
               .isNotNull(UserCollection::getKnowledgePointId)
               .orderByDesc(UserCollection::getCreateTime);
        
        Page<UserCollection> collectionPage = userCollectionMapper.selectPage(page, wrapper);
        
        if (collectionPage.getRecords().isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }
        
        List<Long> kpIds = collectionPage.getRecords().stream()
            .map(UserCollection::getKnowledgePointId)
            .collect(Collectors.toList());
        
        List<KnowledgePoint> knowledgePoints = knowledgePointMapper.selectBatchIds(kpIds);
        List<KnowledgePointVO> vos = knowledgePoints.stream()
            .map(kp -> convertToVO(kp, userId))
            .collect(Collectors.toList());
        
        Page<KnowledgePointVO> result = new Page<>(pageNum, pageSize);
        result.setRecords(vos);
        result.setTotal(collectionPage.getTotal());
        return result;
    }

    @Override
    @Transactional
    public void createKnowledgePoint(KnowledgePointVO knowledgePointVO, Long userId) {
        KnowledgePoint knowledgePoint = new KnowledgePoint();
        BeanUtils.copyProperties(knowledgePointVO, knowledgePoint);
        
        try {
            if (knowledgePointVO.getTags() != null && !knowledgePointVO.getTags().isEmpty()) {
                knowledgePoint.setTags(objectMapper.writeValueAsString(knowledgePointVO.getTags()));
            }
            if (knowledgePointVO.getRelatedPoints() != null) {
                knowledgePoint.setRelatedPoints(objectMapper.writeValueAsString(knowledgePointVO.getRelatedPoints()));
            }
            if (knowledgePointVO.getPrerequisites() != null) {
                knowledgePoint.setPrerequisites(objectMapper.writeValueAsString(knowledgePointVO.getPrerequisites()));
            }
        } catch (Exception e) {
            throw new RuntimeException("JSON转换失败", e);
        }
        
        knowledgePoint.setCreatorId(userId);
        knowledgePoint.setStatus(2); // 待审核
        knowledgePoint.setViewCount(0);
        knowledgePoint.setCollectCount(0);
        knowledgePoint.setAvgRating(0.0);
        knowledgePoint.setRatingCount(0);
        
        knowledgePointMapper.insert(knowledgePoint);
    }

    @Override
    public Page<KnowledgePointVO> getAdminKnowledgePointList(KnowledgePointQueryDTO queryDTO) {
        Page<KnowledgePoint> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<KnowledgePoint> wrapper = buildQueryWrapper(queryDTO);
        
        Page<KnowledgePoint> resultPage = knowledgePointMapper.selectPage(page, wrapper);
        return convertToVOPage(resultPage, null);
    }

    @Override
    @Transactional
    public void auditKnowledgePoint(Long id, Integer status, String reason) {
        LambdaUpdateWrapper<KnowledgePoint> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(KnowledgePoint::getStatus, status)
               .set(StringUtils.hasText(reason), KnowledgePoint::getAuditReason, reason)
               .eq(KnowledgePoint::getId, id);
        
        knowledgePointMapper.update(null, wrapper);
    }

    @Override
    @Transactional
    public void updateKnowledgePointStatus(Long id, Integer status) {
        // 查询当前知识点状态
        KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(id);
        if (knowledgePoint == null) {
            throw new RuntimeException("知识点不存在");
        }
        
        // 校验状态是否相同
        if (knowledgePoint.getStatus().equals(status)) {
            String statusText = status == 1 ? "上架" : (status == 0 ? "下架" : "待审核");
            throw new RuntimeException("该知识点已经是" + statusText + "状态");
        }
        
        LambdaUpdateWrapper<KnowledgePoint> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(KnowledgePoint::getStatus, status)
               .eq(KnowledgePoint::getId, id);
        
        knowledgePointMapper.update(null, wrapper);
    }

    @Override
    @Transactional
    public void deleteKnowledgePoint(Long id) {
        knowledgePointMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDeleteKnowledgePoint(Long[] ids) {
        knowledgePointMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    @Transactional
    public void batchUpdateStatus(Long[] ids, Integer status) {
        // 批量更新前过滤掉已是目标状态的
        LambdaQueryWrapper<KnowledgePoint> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(KnowledgePoint::getId, Arrays.asList(ids))
                   .ne(KnowledgePoint::getStatus, status);
        
        List<KnowledgePoint> needUpdateList = knowledgePointMapper.selectList(queryWrapper);
        
        if (needUpdateList.isEmpty()) {
            String statusText = status == 1 ? "上架" : (status == 0 ? "下架" : "待审核");
            throw new RuntimeException("所选知识点已经是" + statusText + "状态");
        }
        
        List<Long> updateIds = needUpdateList.stream()
                .map(KnowledgePoint::getId)
                .collect(Collectors.toList());
        
        LambdaUpdateWrapper<KnowledgePoint> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(KnowledgePoint::getStatus, status)
               .in(KnowledgePoint::getId, updateIds);
        
        knowledgePointMapper.update(null, wrapper);
    }

    @Override
    @Transactional
    public void updateKnowledgePointInfo(Long id, KnowledgePointVO knowledgePointVO) {
        KnowledgePoint knowledgePoint = new KnowledgePoint();
        BeanUtils.copyProperties(knowledgePointVO, knowledgePoint);
        knowledgePoint.setId(id);
        
        try {
            if (knowledgePointVO.getTags() != null) {
                knowledgePoint.setTags(objectMapper.writeValueAsString(knowledgePointVO.getTags()));
            }
            if (knowledgePointVO.getRelatedPoints() != null) {
                knowledgePoint.setRelatedPoints(objectMapper.writeValueAsString(knowledgePointVO.getRelatedPoints()));
            }
            if (knowledgePointVO.getPrerequisites() != null) {
                knowledgePoint.setPrerequisites(objectMapper.writeValueAsString(knowledgePointVO.getPrerequisites()));
            }
        } catch (Exception e) {
            throw new RuntimeException("JSON转换失败", e);
        }
        
        knowledgePointMapper.updateById(knowledgePoint);
    }

    @Override
    public Map<String, Object> getKnowledgePointStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总数
        Long total = knowledgePointMapper.selectCount(null);
        stats.put("total", total);
        
        // 上架
        Long online = knowledgePointMapper.selectCount(
            new LambdaQueryWrapper<KnowledgePoint>().eq(KnowledgePoint::getStatus, 1)
        );
        stats.put("online", online);
        
        // 下架
        Long offline = knowledgePointMapper.selectCount(
            new LambdaQueryWrapper<KnowledgePoint>().eq(KnowledgePoint::getStatus, 0)
        );
        stats.put("offline", offline);
        
        // 待审核
        Long pending = knowledgePointMapper.selectCount(
            new LambdaQueryWrapper<KnowledgePoint>().eq(KnowledgePoint::getStatus, 2)
        );
        stats.put("pending", pending);
        
        return stats;
    }
    
    // ========== 私有辅助方法 ==========
    
    private LambdaQueryWrapper<KnowledgePoint> buildQueryWrapper(KnowledgePointQueryDTO queryDTO) {
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(KnowledgePoint::getTitle, queryDTO.getKeyword())
                             .or().like(KnowledgePoint::getDescription, queryDTO.getKeyword())
                             .or().like(KnowledgePoint::getContent, queryDTO.getKeyword()));
        }
        
        // 分类筛选
        if (StringUtils.hasText(queryDTO.getCategory())) {
            wrapper.eq(KnowledgePoint::getCategory, queryDTO.getCategory());
        }
        
        // 子分类筛选
        if (StringUtils.hasText(queryDTO.getSubCategory())) {
            wrapper.eq(KnowledgePoint::getSubCategory, queryDTO.getSubCategory());
        }
        
        // 难度筛选
        if (StringUtils.hasText(queryDTO.getDifficulty())) {
            wrapper.eq(KnowledgePoint::getDifficulty, queryDTO.getDifficulty());
        }
        
        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(KnowledgePoint::getStatus, queryDTO.getStatus());
        }
        
        // 排序
        String orderBy = StringUtils.hasText(queryDTO.getOrderBy()) ? queryDTO.getOrderBy() : "createTime";
        boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getOrderType());
        
        switch (orderBy) {
            case "viewCount":
                wrapper.orderBy(true, isAsc, KnowledgePoint::getViewCount);
                break;
            case "avgRating":
                wrapper.orderBy(true, isAsc, KnowledgePoint::getAvgRating);
                break;
            default:
                wrapper.orderBy(true, isAsc, KnowledgePoint::getCreateTime);
                break;
        }
        
        return wrapper;
    }
    
    private Page<KnowledgePointVO> convertToVOPage(Page<KnowledgePoint> page, Long userId) {
        Page<KnowledgePointVO> voPage = new Page<>(page.getCurrent(), page.getSize());
        voPage.setTotal(page.getTotal());
        
        List<KnowledgePointVO> vos = page.getRecords().stream()
            .map(kp -> convertToVO(kp, userId))
            .collect(Collectors.toList());
        
        voPage.setRecords(vos);
        return voPage;
    }
    
    private KnowledgePointVO convertToVO(KnowledgePoint kp, Long userId) {
        KnowledgePointVO vo = new KnowledgePointVO();
        BeanUtils.copyProperties(kp, vo);
        
        try {
            // 解析JSON字段
            if (StringUtils.hasText(kp.getTags())) {
                vo.setTags(objectMapper.readValue(kp.getTags(), new TypeReference<List<String>>(){}));
            }
            if (StringUtils.hasText(kp.getRelatedPoints())) {
                vo.setRelatedPoints(objectMapper.readValue(kp.getRelatedPoints(), new TypeReference<List<String>>(){}));
            }
            if (StringUtils.hasText(kp.getPrerequisites())) {
                vo.setPrerequisites(objectMapper.readValue(kp.getPrerequisites(), new TypeReference<List<String>>(){}));
            }
        } catch (Exception e) {
            // JSON解析失败时使用空列表
        }
        
        // 设置默认值
        vo.setIsCollected(false);
        vo.setUserRating(null);
        
        return vo;
    }
    
    /**
     * 批量设置用户相关属性
     */
    public void batchSetUserAttributes(List<KnowledgePointVO> vos, Long userId) {
        if (userId == null || vos == null || vos.isEmpty()) {
            return;
        }
        
        // 提取所有知识点ID
        List<Long> kpIds = vos.stream()
            .map(KnowledgePointVO::getId)
            .collect(Collectors.toList());
        
        // 批量查询收藏状态
        LambdaQueryWrapper<UserCollection> collectionWrapper = new LambdaQueryWrapper<>();
        collectionWrapper.eq(UserCollection::getUserId, userId)
                       .in(UserCollection::getKnowledgePointId, kpIds);
        List<UserCollection> collections = userCollectionMapper.selectList(collectionWrapper);
        Set<Long> collectedIds = collections.stream()
            .map(UserCollection::getKnowledgePointId)
            .collect(Collectors.toSet());
        
        // 批量查询评分
        LambdaQueryWrapper<UserBehavior> ratingWrapper = new LambdaQueryWrapper<>();
        ratingWrapper.eq(UserBehavior::getUserId, userId)
                     .in(UserBehavior::getKnowledgePointId, kpIds)
                     .eq(UserBehavior::getBehaviorType, "rate")
                     .orderByDesc(UserBehavior::getCreateTime);
        List<UserBehavior> ratings = userBehaviorMapper.selectList(ratingWrapper);
        Map<Long, Integer> ratingMap = ratings.stream()
            .collect(Collectors.toMap(
                UserBehavior::getKnowledgePointId, 
                rating -> rating.getRating().intValue(), // 确保转换为Integer
                (existing, replacement) -> existing // 如果有多个评分，保留第一个
            ));
        
        // 设置用户相关属性
        for (KnowledgePointVO vo : vos) {
            vo.setIsCollected(collectedIds.contains(vo.getId()));
            vo.setUserRating(ratingMap.get(vo.getId()));
        }
    }
    
    @Override
    @Cacheable(value = "knowledgePointCache", key = "{#ids, #userId}")
    public List<KnowledgePointVO> getBatchKnowledgePointDetails(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询知识点数据
        List<KnowledgePoint> knowledgePoints = knowledgePointMapper.selectBatchIds(ids);
        
        // 转换为VO并设置用户相关属性
        List<KnowledgePointVO> vos = knowledgePoints.stream()
            .map(kp -> convertToVO(kp, userId))
            .collect(Collectors.toList());
        
        // 批量设置用户相关属性
        batchSetUserAttributes(vos, userId);
        
        return vos;
    }
    
    @Override
    @Transactional
    public void batchUpdateKnowledgePoints(List<KnowledgePointVO> knowledgePoints, Long userId) {
        if (knowledgePoints == null || knowledgePoints.isEmpty()) {
            return;
        }
        
        // 批量验证权限
        for (KnowledgePointVO vo : knowledgePoints) {
            KnowledgePoint existing = knowledgePointMapper.selectById(vo.getId());
            if (existing == null) {
                throw new RuntimeException("知识点不存在: " + vo.getId());
            }
            
            // 验证权限：只有管理员才能编辑
            // 这里可以根据具体权限逻辑进行判断
        }
        
        // 批量更新知识点
        for (KnowledgePointVO vo : knowledgePoints) {
            KnowledgePoint kp = new KnowledgePoint();
            BeanUtils.copyProperties(vo, kp);
            
            try {
                if (vo.getTags() != null) {
                    kp.setTags(objectMapper.writeValueAsString(vo.getTags()));
                }
                if (vo.getRelatedPoints() != null) {
                    kp.setRelatedPoints(objectMapper.writeValueAsString(vo.getRelatedPoints()));
                }
                if (vo.getPrerequisites() != null) {
                    kp.setPrerequisites(objectMapper.writeValueAsString(vo.getPrerequisites()));
                }
            } catch (Exception e) {
                throw new RuntimeException("JSON转换失败", e);
            }
            
            knowledgePointMapper.updateById(kp);
        }
    }
    
    @Override
    public Map<Long, List<String>> batchValidateContent(List<KnowledgePointVO> requests) {
        Map<Long, List<String>> validationErrors = new HashMap<>();
        
        if (requests == null || requests.isEmpty()) {
            return validationErrors;
        }
        
        for (KnowledgePointVO request : requests) {
            List<String> errors = new ArrayList<>();
            
            // 验证标题
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                errors.add("标题不能为空");
            } else if (request.getTitle().length() > 200) {
                errors.add("标题长度不能超过200字符");
            }
            
            // 验证内容
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                errors.add("内容不能为空");
            }
            
            // 验证描述
            if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
                errors.add("描述不能为空");
            } else if (request.getDescription().length() > 500) {
                errors.add("描述长度不能超过500字符");
            }
            
            // 验证分类
            if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
                errors.add("分类不能为空");
            }
            
            // 验证难度
            if (request.getDifficulty() == null || request.getDifficulty().trim().isEmpty()) {
                errors.add("难度不能为空");
            }
            
            if (!errors.isEmpty()) {
                validationErrors.put(request.getId() != null ? request.getId() : -1L, errors);
            }
        }
        
        return validationErrors;
    }

    @Override
    @Transactional
    public void updateKnowledgePointContent(Long id, String content, Long userId) {
        // 验证知识点是否存在
        KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(id);
        if (knowledgePoint == null) {
            throw new RuntimeException("知识点不存在");
        }

        // 更新内容
        knowledgePoint.setContent(content);
        knowledgePoint.setUpdateTime(LocalDateTime.now());

        // 如果提供了userId，记录更新者
        if (userId != null) {
            knowledgePoint.setUpdatedBy(userId);
        }

        knowledgePointMapper.updateById(knowledgePoint);
    }

    @Override
    @Transactional
    public void submitContentReview(Long contentId, String content, Long userId) {
        // 验证知识点是否存在
        KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(contentId);
        if (knowledgePoint == null) {
            throw new RuntimeException("知识点不存在");
        }

        // 创建审核记录（这里假设有一个审核记录表）
        // 实际项目中需要创建一个ContentReview实体和对应的Mapper
        
        // 为了演示，我们在这里简单地记录一个审核状态
        // 在实际项目中，应该创建一个专门的审核记录表来跟踪审核流程
        knowledgePoint.setAuditStatus(0); // 0表示待审核
        knowledgePoint.setPendingContent(content); // 临时存储待审核的内容
        knowledgePoint.setUpdateTime(LocalDateTime.now());
        knowledgePoint.setUpdatedBy(userId);

        knowledgePointMapper.updateById(knowledgePoint);
    }

    @Override
    public Page<KnowledgePointVO> getPendingReviewKnowledgePoints(Integer pageNum, Integer pageSize) {
        Page<KnowledgePoint> page = new Page<>(pageNum, pageSize);
        // 查询audit_status为0（待审核）的知识点
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePoint::getAuditStatus, 0) // 待审核状态
               .isNotNull(KnowledgePoint::getPendingContent) // 有待审核的内容
               .orderByDesc(KnowledgePoint::getUpdateTime); // 按更新时间倒序

        Page<KnowledgePoint> resultPage = knowledgePointMapper.selectPage(page, wrapper);
        return convertToVOPage(resultPage, null);
    }

    @Override
    @Transactional
    public void reviewKnowledgePointContent(Long id, Integer status, String reason, Long reviewerId) {
        // 验证知识点是否存在
        KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(id);
        if (knowledgePoint == null) {
            throw new RuntimeException("知识点不存在");
        }

        // 检查当前是否处于待审核状态
        if (knowledgePoint.getAuditStatus() != 0) {
            throw new RuntimeException("知识点不在待审核状态，无法进行审核操作");
        }

        // 根据审核结果处理
        if (status == 1) {
            // 审核通过：更新为待审核的内容
            if (knowledgePoint.getPendingContent() != null) {
                knowledgePoint.setContent(knowledgePoint.getPendingContent());
                knowledgePoint.setPendingContent(null); // 清空待审核内容
            }
            knowledgePoint.setAuditStatus(1); // 1表示审核通过
        } else if (status == 2) {
            // 审核拒绝：清空待审核内容，保留原内容
            knowledgePoint.setPendingContent(null);
            knowledgePoint.setAuditStatus(2); // 2表示审核拒绝
        } else {
            throw new RuntimeException("审核状态不正确，只能是1（通过）或2（拒绝）");
        }

        // 设置审核相关信息
        knowledgePoint.setAuditReason(reason);
        knowledgePoint.setUpdatedBy(reviewerId);
        knowledgePoint.setUpdateTime(LocalDateTime.now());

        // 更新知识点信息
        knowledgePointMapper.updateById(knowledgePoint);
    }
}
