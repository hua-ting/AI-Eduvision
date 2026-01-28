package com.learning.recommend.crawler.impl;

import com.learning.recommend.crawler.BaseCrawler;
import com.learning.recommend.vo.CrawlerResultVO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜鸟教程爬虫实现
 */
@Component
public class RunoobCrawler extends BaseCrawler {
    
    private static final String BASE_URL = "https://www.runoob.com";
    
    @Override
    public List<CrawlerResultVO> search(String keyword, int page, int pageSize) throws Exception {
        List<CrawlerResultVO> results = new ArrayList<>();
        
        try {
            // 菜鸟教程主要是教程列表，我们抓取首页的教程分类
            Document doc = getDocument(BASE_URL);
            
            // 获取所有教程链接
            Elements tutorials = doc.select(".item-top a");
            
            int count = 0;
            for (Element tutorial : tutorials) {
                if (count >= pageSize) break;
                
                try {
                    String title = tutorial.text().trim();
                    String href = tutorial.attr("href");
                    
                    // 关键词匹配
                    if (keyword != null && !keyword.isEmpty()) {
                        if (!title.toLowerCase().contains(keyword.toLowerCase())) {
                            continue;
                        }
                    }
                    
                    CrawlerResultVO vo = new CrawlerResultVO();
                    vo.setTitle(title + " 教程");
                    vo.setUrl(href.startsWith("http") ? href : BASE_URL + href);
                    vo.setDescription("菜鸟教程提供的" + title + "学习资料，适合初学者入门学习");
                    vo.setAuthor("菜鸟教程");
                    vo.setSource("菜鸟教程");
                    vo.setCategory(inferCategory(title));
                    vo.setDifficulty("初级");
                    vo.setPublishTime("2024");
                    
                    results.add(vo);
                    count++;
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            throw new Exception("菜鸟教程搜索失败: " + e.getMessage());
        }
        
        return results;
    }
    
    @Override
    public String getType() {
        return "runoob";
    }
    
    private String inferCategory(String title) {
        if (title == null) return "其他";
        
        title = title.toLowerCase();
        if (title.contains("java") || title.contains("python") || title.contains("javascript")
            || title.contains("c++") || title.contains("go") || title.contains("php")) {
            return "编程语言";
        } else if (title.contains("mysql") || title.contains("sql") || title.contains("redis")
            || title.contains("mongodb")) {
            return "数据库";
        } else if (title.contains("html") || title.contains("css") || title.contains("vue")
            || title.contains("react")) {
            return "前端开发";
        } else if (title.contains("linux") || title.contains("docker") || title.contains("git")) {
            return "运维";
        }
        return "编程语言";
    }
}
