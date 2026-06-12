package com.ruoyi.requirement.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.dto.ReqActionInstruction;
import com.ruoyi.requirement.dto.ReqDemandSupplementRequest;
import com.ruoyi.requirement.service.IReqDemandService;

@RestController
@RequestMapping("/requirement/demand")
public class ReqDemandController extends BaseController
{
    private static final long MAX_DEMAND_UPLOAD_SIZE = 2 * 1024 * 1024L;

    @Autowired
    private IReqDemandService reqDemandService;

    @Autowired
    private ServerConfig serverConfig;

    @PreAuthorize("@ss.hasPermi('req:demand:list')")
    @GetMapping("/list")
    public TableDataInfo list(ReqDemand reqDemand)
    {
        startPage();
        List<ReqDemand> list = reqDemandService.selectReqDemandList(reqDemand);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyPermi('req:demand:list,req:demand:add,req:demand:edit,req:demand:query')")
    @GetMapping("/developer-options")
    public AjaxResult developerOptions(String userName)
    {
        return success(reqDemandService.selectDeveloperOptions(userName));
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

    @PreAuthorize("@ss.hasPermi('req:demand:remove')")
    @Log(title = "需求", businessType = BusinessType.DELETE)
    @DeleteMapping("/{demandIds}")
    public AjaxResult remove(@PathVariable Long[] demandIds)
    {
        return toAjax(reqDemandService.deleteReqDemandByDemandIds(demandIds, getUsername()));
    }

    @PreAuthorize("@ss.hasAnyPermi('req:demand:add,req:demand:edit')")
    @Log(title = "需求附件", businessType = BusinessType.INSERT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult uploadFile(MultipartFile file) throws Exception
    {
        try
        {
            validateDemandUpload(file);
            String filePath = RuoYiConfig.getUploadPath();
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", fileName);
            ajax.put("newFileName", FileUtils.getName(fileName));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('req:demand:edit')")
    @Log(title = "需求状态", businessType = BusinessType.UPDATE)
    @PostMapping("/{demandId}/status/{status}")
    public AjaxResult updateStatus(@PathVariable Long demandId, @PathVariable String status)
    {
        return toAjax(reqDemandService.updateReqDemandStatus(demandId, status, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('req:demand:edit')")
    @Log(title = "需求补充说明", businessType = BusinessType.UPDATE)
    @PostMapping("/{demandId}/supplement")
    public AjaxResult submitSupplement(@PathVariable Long demandId, @RequestBody ReqDemandSupplementRequest request)
    {
        return toAjax(reqDemandService.submitDemandSupplement(demandId,
                request == null ? null : request.getContent(), getUsername()));
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

    private void validateDemandUpload(MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > MAX_DEMAND_UPLOAD_SIZE)
        {
            throw new IllegalArgumentException("上传文件大小不能超过 2MB");
        }
    }
}
