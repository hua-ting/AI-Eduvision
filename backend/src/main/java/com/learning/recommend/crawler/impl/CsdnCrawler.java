package com.learning.recommend.crawler.impl;

import com.learning.recommend.crawler.BaseCrawler;
import com.learning.recommend.vo.CrawlerResultVO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CSDN爬虫实现
 */
@Component
public class CsdnCrawler extends BaseCrawler {
    
    private static final String SEARCH_URL = "https://so.csdn.net/so/search?q=%s&p=%d";
    
    @Override
    public List<CrawlerResultVO> search(String keyword, int page, int pageSize) throws Exception {
        List<CrawlerResultVO> results = new ArrayList<>();
        
        try {
            // 构建搜索URL
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = String.format(SEARCH_URL, encodedKeyword, page);
            
            // 获取页面
            Document doc = getDocument(url);
            
            // 解析搜索结果
            Elements items = doc.select(".search-list .search-list-item");
            
            int count = 0;
            for (Element item : items) {
                if (count >= pageSize) break;
                
                try {
                    CrawlerResultVO vo = new CrawlerResultVO();
                    
                    // 标题和链接
                    Element titleElement = item.selectFirst("h2 a");
                    if (titleElement != null) {
                        vo.setTitle(titleElement.text().trim());
                        vo.setUrl(titleElement.attr("href"));
                    }
                    
                    // 描述
                    Element descElement = item.selectFirst(".search-item-content");
                    if (descElement != null) {
                        vo.setDescription(descElement.text().trim());
                    }
                    
                    // 作者
                    Element authorElement = item.selectFirst(".search-item-user a");
                    if (authorElement != null) {
                        vo.setAuthor(authorElement.text().trim());
                    }
                    
                    // 时间
                    Element timeElement = item.selectFirst(".search-item-time");
                    if (timeElement != null) {
                        vo.setPublishTime(timeElement.text().trim());
                    }
                    
                    vo.setSource("CSDN");
                    vo.setCategory(inferCategory(keyword));
                    vo.setDifficulty(inferDifficulty(vo.getTitle(), vo.getDescription()));
                    
                    // 只添加有效结果
                    if (vo.getTitle() != null && vo.getUrl() != null) {
                        results.add(vo);
                        count++;
                    }
                } catch (Exception e) {
                    // 跳过解析失败的单条记录
                    continue;
                }
            }
        } catch (Exception e) {
            throw new Exception("CSDN搜索失败: " + e.getMessage());
        }
        
        return results;
    }
    
    @Override
    public String getType() {
        return "csdn";
    }
    
    /**
     * 推断分类
     */
    private String inferCategory(String keyword) {
        if (keyword == null) return "其他";
        
        keyword = keyword.toLowerCase();
        if (keyword.contains("java") || keyword.contains("python") || keyword.contains("javascript") 
            || keyword.contains("c++") || keyword.contains("go")) {
            return "编程语言";
        } else if (keyword.contains("数据库") || keyword.contains("mysql") || keyword.contains("redis")
            || keyword.contains("mongodb")) {
            return "数据库";
        } else if (keyword.contains("算法") || keyword.contains("数据结构") || keyword.contains("leetcode")) {
            return "算法";
        } else if (keyword.contains("前端") || keyword.contains("vue") || keyword.contains("react")
            || keyword.contains("html") || keyword.contains("css")) {
            return "前端开发";
        } else if (keyword.contains("机器学习") || keyword.contains("深度学习") || keyword.contains("ai")
            || keyword.contains("神经网络")) {
            return "人工智能";
        } else if (keyword.contains("spring") || keyword.contains("框架") || keyword.contains("mybatis")) {
            return "框架";
        }
        return "其他";
    }
    
    /**
     * 推断难度
     */
    private String inferDifficulty(String title, String description) {
        String text = (title + " " + description).toLowerCase();
        
        if (text.contains("入门") || text.contains("基础") || text.contains("初学") 
            || text.contains("新手") || text.contains("从零")) {
            return "初级";
        } else if (text.contains("高级") || text.contains("深入") || text.contains("进阶")
            || text.contains("原理") || text.contains("源码")) {
            return "高级";
        }
        return "中级";
    }
}
