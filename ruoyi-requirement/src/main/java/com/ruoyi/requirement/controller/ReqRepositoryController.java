package com.ruoyi.requirement.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.service.IReqRepositoryService;

/**
 * 需求平台仓库Controller
 */
@RestController
@RequestMapping("/requirement/repository")
public class ReqRepositoryController extends BaseController
{
    @Autowired
    private IReqRepositoryService reqRepositoryService;

    @PreAuthorize("@ss.hasPermi('req:repo:list')")
    @GetMapping("/list")
    public TableDataInfo list(ReqRepository reqRepository)
    {
        startPage();
        List<ReqRepository> list = reqRepositoryService.selectReqRepositoryList(reqRepository);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('req:repo:query')")
    @GetMapping(value = "/{repoId}")
    public AjaxResult getInfo(@PathVariable("repoId") Long repoId)
    {
        return success(reqRepositoryService.selectReqRepositoryByRepoId(repoId));
    }

    @PreAuthorize("@ss.hasPermi('req:repo:add')")
    @Log(title = "需求平台仓库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReqRepository reqRepository)
    {
        reqRepository.setCreateBy(getUsername());
        return toAjax(reqRepositoryService.insertReqRepository(reqRepository));
    }

    @PreAuthorize("@ss.hasPermi('req:repo:edit')")
    @Log(title = "需求平台仓库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ReqRepository reqRepository)
    {
        reqRepository.setUpdateBy(getUsername());
        return toAjax(reqRepositoryService.updateReqRepository(reqRepository));
    }

    @PreAuthorize("@ss.hasPermi('req:repo:remove')")
    @Log(title = "需求平台仓库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{repoIds}")
    public AjaxResult remove(@PathVariable Long[] repoIds)
    {
        return toAjax(reqRepositoryService.deleteReqRepositoryByRepoIds(repoIds));
    }
}
