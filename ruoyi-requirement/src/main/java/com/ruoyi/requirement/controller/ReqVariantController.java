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
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.service.IReqVariantService;

/**
 * 项目分支兼容Controller
 */
@RestController
@RequestMapping("/requirement/variant")
public class ReqVariantController extends BaseController
{
    @Autowired
    private IReqVariantService reqVariantService;

    @PreAuthorize("@ss.hasAnyPermi('req:variant:list,req:demand:list,req:demand:add,req:demand:edit,req:demand:query')")
    @GetMapping("/list")
    public TableDataInfo list(ReqVariant reqVariant)
    {
        startPage();
        List<ReqVariant> list = reqVariantService.selectReqVariantList(reqVariant);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyPermi('req:variant:query,req:demand:add,req:demand:edit,req:demand:query')")
    @GetMapping(value = "/{variantId}")
    public AjaxResult getInfo(@PathVariable("variantId") Long variantId)
    {
        return success(reqVariantService.selectReqVariantByVariantId(variantId));
    }

    @PreAuthorize("@ss.hasPermi('req:variant:add')")
    @Log(title = "项目分支", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReqVariant reqVariant)
    {
        reqVariant.setCreateBy(getUsername());
        return toAjax(reqVariantService.insertReqVariant(reqVariant));
    }

    @PreAuthorize("@ss.hasPermi('req:variant:edit')")
    @Log(title = "项目分支", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ReqVariant reqVariant)
    {
        reqVariant.setUpdateBy(getUsername());
        return toAjax(reqVariantService.updateReqVariant(reqVariant));
    }

    @PreAuthorize("@ss.hasPermi('req:variant:remove')")
    @Log(title = "项目分支", businessType = BusinessType.DELETE)
    @DeleteMapping("/{variantIds}")
    public AjaxResult remove(@PathVariable Long[] variantIds)
    {
        return toAjax(reqVariantService.deleteReqVariantByVariantIds(variantIds));
    }
}
