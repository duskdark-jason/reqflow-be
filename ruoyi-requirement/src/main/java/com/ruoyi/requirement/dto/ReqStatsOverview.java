package com.ruoyi.requirement.dto;

public class ReqStatsOverview
{
    private Long demandCount;
    private Long packageCount;
    private Long planCount;
    private Long executionReportCount;
    private Long reviewReportCount;
    private Long activeUserCount;

    public Long getDemandCount() { return demandCount; }
    public void setDemandCount(Long demandCount) { this.demandCount = demandCount; }
    public Long getPackageCount() { return packageCount; }
    public void setPackageCount(Long packageCount) { this.packageCount = packageCount; }
    public Long getPlanCount() { return planCount; }
    public void setPlanCount(Long planCount) { this.planCount = planCount; }
    public Long getExecutionReportCount() { return executionReportCount; }
    public void setExecutionReportCount(Long executionReportCount) { this.executionReportCount = executionReportCount; }
    public Long getReviewReportCount() { return reviewReportCount; }
    public void setReviewReportCount(Long reviewReportCount) { this.reviewReportCount = reviewReportCount; }
    public Long getActiveUserCount() { return activeUserCount; }
    public void setActiveUserCount(Long activeUserCount) { this.activeUserCount = activeUserCount; }
}
