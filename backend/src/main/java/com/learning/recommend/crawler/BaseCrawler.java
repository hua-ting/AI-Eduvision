package com.learning.recommend.crawler;

import com.learning.recommend.vo.CrawlerResultVO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;

/**
 * 爬虫基类
 */
public abstract class BaseCrawler {
    
    @Value("${material.crawler.user-agent}")
    protected String userAgent;
    
    @Value("${material.crawler.timeout}")
    protected int timeout;
    
    /**
     * 获取HTML文档
     */
    protected Document getDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(timeout)
                .get();
    }
    
    /**
     * 搜索资料
     */
    public abstract List<CrawlerResultVO> search(String keyword, int page, int pageSize) throws Exception;
    
    /**
     * 获取爬虫类型
     */
    public abstract String getType();
}
