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
import com.ruoyi.requirement.domain.ReqModule;
import com.ruoyi.requirement.service.IReqModuleService;

/**
 * 模块功能点Controller
 */
@RestController
@RequestMapping("/requirement/module")
public class ReqModuleController extends BaseController
{
    @Autowired
    private IReqModuleService reqModuleService;

    @PreAuthorize("@ss.hasAnyPermi('req:module:list,req:demand:list,req:demand:add,req:demand:edit,req:demand:query')")
    @GetMapping("/list")
    public TableDataInfo list(ReqModule reqModule)
    {
        startPage();
        List<ReqModule> list = reqModuleService.selectReqModuleList(reqModule);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyPermi('req:module:query,req:demand:add,req:demand:edit,req:demand:query')")
    @GetMapping(value = "/{moduleId}")
    public AjaxResult getInfo(@PathVariable("moduleId") Long moduleId)
    {
        return success(reqModuleService.selectReqModuleByModuleId(moduleId));
    }

    @PreAuthorize("@ss.hasPermi('req:module:add')")
    @Log(title = "模块功能点", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReqModule reqModule)
    {
        reqModule.setCreateBy(getUsername());
        return toAjax(reqModuleService.insertReqModule(reqModule));
    }

    @PreAuthorize("@ss.hasPermi('req:module:edit')")
    @Log(title = "模块功能点", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ReqModule reqModule)
    {
        reqModule.setUpdateBy(getUsername());
        return toAjax(reqModuleService.updateReqModule(reqModule));
    }

    @PreAuthorize("@ss.hasPermi('req:module:remove')")
    @Log(title = "模块功能点", businessType = BusinessType.DELETE)
    @DeleteMapping("/{moduleIds}")
    public AjaxResult remove(@PathVariable Long[] moduleIds)
    {
        return toAjax(reqModuleService.deleteReqModuleByModuleIds(moduleIds));
    }
}
