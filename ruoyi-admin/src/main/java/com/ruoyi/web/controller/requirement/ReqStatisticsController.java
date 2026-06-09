package com.ruoyi.web.controller.requirement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.requirement.service.ReqStatisticsService;

@RestController
@RequestMapping("/requirement/statistics")
public class ReqStatisticsController extends BaseController
{
    @Autowired
    private ReqStatisticsService reqStatisticsService;

    @PreAuthorize("@ss.hasPermi('req:stats:view')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return success(reqStatisticsService.overview());
    }

    @PreAuthorize("@ss.hasPermi('req:stats:view')")
    @GetMapping("/project-rank")
    public AjaxResult projectRank()
    {
        return success(reqStatisticsService.projectRank());
    }

    @PreAuthorize("@ss.hasPermi('req:stats:view')")
    @GetMapping("/user-usage")
    public AjaxResult userUsage()
    {
        return success(reqStatisticsService.userUsage());
    }
}
