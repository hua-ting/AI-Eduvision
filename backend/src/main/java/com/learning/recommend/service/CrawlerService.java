package com.learning.recommend.service;

import com.learning.recommend.dto.CrawlerSearchDTO;
import com.learning.recommend.vo.CrawlerResultVO;

import java.util.List;

/**
 * 资料采集服务接口
 */
public interface CrawlerService {
    
    /**
     * 搜索资料
     */
    List<CrawlerResultVO> searchMaterials(CrawlerSearchDTO searchDTO);
    
    /**
     * 批量导入资料
     */
    void importMaterials(List<CrawlerResultVO> materials);
    
    /**
     * 获取可用的资料源列表
     */
    List<String> getAvailableSources();
}
