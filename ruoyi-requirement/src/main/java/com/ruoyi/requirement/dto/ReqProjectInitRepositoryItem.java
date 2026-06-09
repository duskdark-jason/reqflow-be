package com.ruoyi.requirement.dto;

import java.util.Date;

public class ReqProjectInitRepositoryItem
{
    private Long repoId;
    private Long projectId;
    private String repoName;
    private String repoType;
    private String repoUrl;
    private String localPathHint;
    private String defaultBranch;
    private String harnessStatus;
    private String harnessCommit;
    private Date lastIndexedAt;
    private String status;
    private String remark;

    public Long getRepoId() { return repoId; }
    public void setRepoId(Long repoId) { this.repoId = repoId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }
    public String getRepoType() { return repoType; }
    public void setRepoType(String repoType) { this.repoType = repoType; }
    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
    public String getLocalPathHint() { return localPathHint; }
    public void setLocalPathHint(String localPathHint) { this.localPathHint = localPathHint; }
    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }
    public String getHarnessStatus() { return harnessStatus; }
    public void setHarnessStatus(String harnessStatus) { this.harnessStatus = harnessStatus; }
    public String getHarnessCommit() { return harnessCommit; }
    public void setHarnessCommit(String harnessCommit) { this.harnessCommit = harnessCommit; }
    public Date getLastIndexedAt() { return lastIndexedAt; }
    public void setLastIndexedAt(Date lastIndexedAt) { this.lastIndexedAt = lastIndexedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
