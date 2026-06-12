package com.ruoyi.requirement.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.requirement.domain.ReqIndexModule;
import com.ruoyi.requirement.domain.ReqRepositoryIndexBatch;
import com.ruoyi.requirement.dto.ReqImpactSuggestQuery;
import com.ruoyi.requirement.dto.ReqRepositoryIndexImportRequest;
import com.ruoyi.requirement.service.IReqRepositoryIndexService;

@RestController
@RequestMapping("/requirement/index")
public class ReqIndexController extends BaseController
{
    @Autowired
    private IReqRepositoryIndexService repositoryIndexService;

    @PreAuthorize("@ss.hasAnyPermi('req:index:list,req:demand:list,req:demand:add,req:demand:edit,req:demand:query')")
    @GetMapping("/batch/list")
    public TableDataInfo batchList(ReqRepositoryIndexBatch batch)
    {
        startPage();
        List<ReqRepositoryIndexBatch> list = repositoryIndexService.selectBatchList(batch);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyPermi('req:index:list,req:demand:add,req:demand:edit,req:demand:query')")
    @GetMapping("/module/tree")
    public AjaxResult moduleTree(ReqIndexModule module)
    {
        return success(repositoryIndexService.selectModuleList(module));
    }

    @PreAuthorize("@ss.hasPermi('req:index:list')")
    @GetMapping("/impact/suggest")
    public AjaxResult suggest(ReqImpactSuggestQuery query)
    {
        return success(repositoryIndexService.suggestImpact(query));
    }

    @PreAuthorize("@ss.hasPermi('req:index:import')")
    @Log(title = "仓库索引导入", businessType = BusinessType.INSERT)
    @PostMapping("/import")
    public AjaxResult importIndex(@RequestBody ReqRepositoryIndexImportRequest request)
    {
        return success(repositoryIndexService.importRepositoryIndex(request, "web", getUsername(), currentUserId()));
    }

    private Long currentUserId()
    {
        try { return SecurityUtils.getUserId(); }
        catch (Exception e) { return 0L; }
    }
}
