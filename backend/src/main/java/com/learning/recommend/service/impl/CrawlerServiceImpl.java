package com.learning.recommend.service.impl;

import cn.hutool.json.JSONUtil;
import com.learning.recommend.common.Constants;
import com.learning.recommend.crawler.BaseCrawler;
import com.learning.recommend.dto.CrawlerSearchDTO;
import com.learning.recommend.entity.LearningMaterial;
import com.learning.recommend.mapper.LearningMaterialMapper;
import com.learning.recommend.service.CrawlerService;
import com.learning.recommend.vo.CrawlerResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 资料采集服务实现（真实爬虫）
 */
@Slf4j
@Service
public class CrawlerServiceImpl implements CrawlerService {

    @Autowired
    private LearningMaterialMapper materialMapper;

    @Autowired
    private List<BaseCrawler> crawlers;

    @Override
    public List<CrawlerResultVO> searchMaterials(CrawlerSearchDTO searchDTO) {
        String keyword = searchDTO.getKeyword();
        String sourceType = searchDTO.getSourceType();
        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 1;
        int pageSize = searchDTO.getPageSize() != null ? searchDTO.getPageSize() : 20;
        
        // 查找对应的爬虫
        BaseCrawler crawler = crawlers.stream()
                .filter(c -> c.getType().equalsIgnoreCase(sourceType))
                .findFirst()
                .orElse(null);
        
        if (crawler == null) {
            log.error("未找到类型为 {} 的爬虫", sourceType);
            return new ArrayList<>();
        }
        
        try {
            log.info("开始爬取: 关键词={}, 来源={}, 页码={}", keyword, sourceType, page);
            List<CrawlerResultVO> results = crawler.search(keyword, page, pageSize);
            log.info("爬取成功: 共 {} 条结果", results.size());
            return results;
        } catch (Exception e) {
            log.error("爬取失败: {}", e.getMessage(), e);
            throw new RuntimeException("搜索失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importMaterials(List<CrawlerResultVO> materials) {
        for (CrawlerResultVO vo : materials) {
            LearningMaterial material = new LearningMaterial();
            material.setTitle(vo.getTitle());
            material.setDescription(vo.getDescription());
            material.setCategory(vo.getCategory());
            material.setDifficulty(vo.getDifficulty());
            material.setFileUrl(vo.getUrl());
            material.setFileType("链接");
            material.setStatus(Constants.MATERIAL_STATUS_PENDING); // 待审核
            material.setViewCount(0);
            material.setCollectCount(0);
            material.setDownloadCount(0);
            material.setAvgRating(0.0);
            material.setRatingCount(0);
            
            // 自动生成标签
            List<String> tags = Arrays.asList(vo.getCategory(), vo.getDifficulty(), vo.getSource());
            material.setTags(JSONUtil.toJsonStr(tags));
            
            materialMapper.insert(material);
        }
    }

    @Override
    public List<String> getAvailableSources() {
        return crawlers.stream()
                .map(BaseCrawler::getType)
                .map(type -> {
                    switch (type) {
                        case "csdn": return "CSDN";
                        case "runoob": return "菜鸟教程";
                        case "juejin": return "掘金";
                        default: return type;
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
