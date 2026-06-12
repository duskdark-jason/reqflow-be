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
import com.ruoyi.requirement.domain.ReqProject;
import com.ruoyi.requirement.service.IReqProjectService;

/**
 * 需求平台项目Controller
 */
@RestController
@RequestMapping("/requirement/project")
public class ReqProjectController extends BaseController
{
    @Autowired
    private IReqProjectService reqProjectService;

    @PreAuthorize("@ss.hasAnyPermi('req:project:list,req:demand:list,req:demand:add,req:demand:edit,req:demand:query')")
    @GetMapping("/list")
    public TableDataInfo list(ReqProject reqProject)
    {
        startPage();
        List<ReqProject> list = reqProjectService.selectReqProjectList(reqProject);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyPermi('req:project:query,req:demand:add,req:demand:edit,req:demand:query')")
    @GetMapping(value = "/{projectId}")
    public AjaxResult getInfo(@PathVariable("projectId") Long projectId)
    {
        return success(reqProjectService.selectReqProjectByProjectId(projectId));
    }

    @PreAuthorize("@ss.hasPermi('req:project:add')")
    @Log(title = "需求平台项目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReqProject reqProject)
    {
        reqProject.setCreateBy(getUsername());
        return toAjax(reqProjectService.insertReqProject(reqProject));
    }

    @PreAuthorize("@ss.hasPermi('req:project:edit')")
    @Log(title = "需求平台项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ReqProject reqProject)
    {
        reqProject.setUpdateBy(getUsername());
        return toAjax(reqProjectService.updateReqProject(reqProject));
    }

    @PreAuthorize("@ss.hasPermi('req:project:remove')")
    @Log(title = "需求平台项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{projectIds}")
    public AjaxResult remove(@PathVariable Long[] projectIds)
    {
        return toAjax(reqProjectService.deleteReqProjectByProjectIds(projectIds));
    }
}
