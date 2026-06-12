package com.ruoyi.requirement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.requirement.dto.ReqPackageSaveRequest;
import com.ruoyi.requirement.service.IReqPackageService;

@RestController
@RequestMapping("/requirement/package")
public class ReqPackageController extends BaseController
{
    @Autowired
    private IReqPackageService reqPackageService;

    @PreAuthorize("@ss.hasAnyPermi('req:package:list,req:demand:query')")
    @GetMapping("/{demandId}")
    public AjaxResult list(@PathVariable Long demandId)
    {
        return success(reqPackageService.selectReqPackageVersionListByDemandId(demandId));
    }

    @PreAuthorize("@ss.hasAnyPermi('req:package:list,req:demand:query')")
    @GetMapping("/{demandId}/{artifactType}/latest")
    public AjaxResult latest(@PathVariable Long demandId, @PathVariable String artifactType)
    {
        return success(reqPackageService.selectLatest(demandId, artifactType));
    }

    @PreAuthorize("@ss.hasPermi('req:package:save')")
    @Log(title = "需求执行包", businessType = BusinessType.INSERT)
    @PostMapping("/{demandId}/{artifactType}")
    public AjaxResult save(@PathVariable Long demandId, @PathVariable String artifactType, @RequestBody ReqPackageSaveRequest request)
    {
        return success(reqPackageService.saveVersion(demandId, artifactType, request.getContent(), request.getVersionNote()));
    }

    @PreAuthorize("@ss.hasPermi('req:package:save')")
    @Log(title = "需求执行包生成", businessType = BusinessType.INSERT)
    @PostMapping("/generate/{demandId}")
    public AjaxResult generate(@PathVariable Long demandId)
    {
        return success(reqPackageService.generateDraftPackage(demandId));
    }
}
