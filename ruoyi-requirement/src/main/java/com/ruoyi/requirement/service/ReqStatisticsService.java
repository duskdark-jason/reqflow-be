package com.ruoyi.requirement.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.requirement.dto.ReqStatsOverview;
import com.ruoyi.requirement.mapper.ReqActivityLogMapper;
import com.ruoyi.requirement.mapper.ReqDemandMapper;
import com.ruoyi.requirement.mapper.ReqPackageVersionMapper;

@Service
public class ReqStatisticsService
{
    @Autowired private ReqDemandMapper reqDemandMapper;
    @Autowired private ReqPackageVersionMapper reqPackageVersionMapper;
    @Autowired private ReqActivityLogMapper reqActivityLogMapper;

    public ReqStatsOverview overview()
    {
        ReqStatsOverview overview = new ReqStatsOverview();
        overview.setDemandCount(reqDemandMapper.selectDemandCount());
        overview.setPackageCount(reqPackageVersionMapper.selectPackageCountByArtifactType("requirement"));
        overview.setPlanCount(reqPackageVersionMapper.selectPackageCountByArtifactType("plan"));
        overview.setExecutionReportCount(reqPackageVersionMapper.selectPackageCountByArtifactType("execution_report"));
        overview.setReviewReportCount(reqPackageVersionMapper.selectPackageCountByArtifactType("review_report"));
        overview.setActiveUserCount(reqActivityLogMapper.selectActiveUserCount());
        return overview;
    }

    public List<Map<String, Object>> projectRank()
    {
        return reqDemandMapper.selectProjectRank();
    }

    public List<Map<String, Object>> userUsage()
    {
        return reqActivityLogMapper.selectUserUsage();
    }
}
