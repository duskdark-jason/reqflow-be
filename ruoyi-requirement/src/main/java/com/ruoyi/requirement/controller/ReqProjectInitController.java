package com.ruoyi.requirement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.requirement.dto.ReqProjectInitRequest;
import com.ruoyi.requirement.service.IReqProjectInitService;

@RestController
@RequestMapping("/requirement/project/init")
public class ReqProjectInitController extends BaseController
{
    @Autowired
    private IReqProjectInitService projectInitService;

    @PreAuthorize("@ss.hasAnyPermi('req:project:query,req:demand:add,req:demand:edit,req:demand:query')")
    @GetMapping("/{projectId}")
    public AjaxResult getInfo(@PathVariable("projectId") Long projectId)
    {
        return success(projectInitService.selectProjectInit(projectId));
    }

    @PreAuthorize("@ss.hasPermi('req:project:add')")
    @Log(title = "项目初始化", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReqProjectInitRequest request)
    {
        return success(projectInitService.insertProjectInit(request, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('req:project:edit')")
    @Log(title = "项目初始化", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ReqProjectInitRequest request)
    {
        return success(projectInitService.updateProjectInit(request, getUsername()));
    }
}
