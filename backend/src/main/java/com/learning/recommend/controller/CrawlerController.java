package com.learning.recommend.controller;

import com.learning.recommend.common.Result;
import com.learning.recommend.dto.CrawlerSearchDTO;
import com.learning.recommend.service.CrawlerService;
import com.learning.recommend.vo.CrawlerResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资料采集控制器（管理员）
 */
@Api(tags = "资料采集管理")
@RestController
@RequestMapping("/admin/crawler")
public class CrawlerController {

    @Autowired
    private CrawlerService crawlerService;

    @ApiOperation("搜索资料")
    @PostMapping("/search")
    public Result<List<CrawlerResultVO>> searchMaterials(@RequestBody CrawlerSearchDTO searchDTO) {
        List<CrawlerResultVO> results = crawlerService.searchMaterials(searchDTO);
        return Result.success(results);
    }

    @ApiOperation("批量导入资料")
    @PostMapping("/import")
    public Result<Void> importMaterials(@RequestBody List<CrawlerResultVO> materials) {
        crawlerService.importMaterials(materials);
        return Result.success("成功导入" + materials.size() + "条资料", null);
    }

    @ApiOperation("获取可用资料源")
    @GetMapping("/sources")
    public Result<List<String>> getAvailableSources() {
        List<String> sources = crawlerService.getAvailableSources();
        return Result.success(sources);
    }
}
