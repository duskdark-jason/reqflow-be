package com.ruoyi.web.controller.requirement;

import java.util.List;
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
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.dto.ReqActionInstruction;
import com.ruoyi.requirement.service.IReqDemandService;

@RestController
@RequestMapping("/requirement/demand")
public class ReqDemandController extends BaseController
{
    @Autowired
    private IReqDemandService reqDemandService;

    @PreAuthorize("@ss.hasPermi('req:demand:list')")
    @GetMapping("/list")
    public TableDataInfo list(ReqDemand reqDemand)
    {
        startPage();
        List<ReqDemand> list = reqDemandService.selectReqDemandList(reqDemand);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('req:demand:query')")
    @GetMapping(value = "/{demandId}")
    public AjaxResult getInfo(@PathVariable("demandId") Long demandId)
    {
        return success(reqDemandService.selectReqDemandByDemandId(demandId));
    }

    @PreAuthorize("@ss.hasPermi('req:demand:add')")
    @Log(title = "需求", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReqDemand reqDemand)
    {
        reqDemand.setCreateBy(getUsername());
        reqDemand.setCreatorId(getUserId());
        return toAjax(reqDemandService.insertReqDemand(reqDemand));
    }

    @PreAuthorize("@ss.hasPermi('req:demand:edit')")
    @Log(title = "需求", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ReqDemand reqDemand)
    {
        reqDemand.setUpdateBy(getUsername());
        reqDemand.setCreatorId(getUserId());
        return toAjax(reqDemandService.updateReqDemand(reqDemand));
    }

    @PreAuthorize("@ss.hasPermi('req:demand:edit')")
    @Log(title = "需求状态", businessType = BusinessType.UPDATE)
    @PostMapping("/{demandId}/status/{status}")
    public AjaxResult updateStatus(@PathVariable Long demandId, @PathVariable String status)
    {
        return toAjax(reqDemandService.updateReqDemandStatus(demandId, status, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('req:demand:query')")
    @GetMapping("/{demandId}/plan-instruction")
    public AjaxResult planInstruction(@PathVariable Long demandId)
    {
        ReqActionInstruction instruction = reqDemandService.createRequirementPlanInstruction(demandId, getUsername());
        return success(instruction);
    }

    @PreAuthorize("@ss.hasPermi('req:demand:query')")
    @GetMapping("/{demandId}/develop-instruction")
    public AjaxResult developInstruction(@PathVariable Long demandId)
    {
        ReqActionInstruction instruction = reqDemandService.createRequirementDevelopInstruction(demandId, getUsername());
        return success(instruction);
    }
}
