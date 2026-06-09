package com.ruoyi.requirement.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 仓库索引批次对象 req_repository_index_batch
 */
public class ReqRepositoryIndexBatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long batchId;
    private Long projectId;
    private Long repoId;
    private String repoType;
    private String remoteUrl;
    private String branchName;
    private String commitHash;
    private String indexVersion;
    private String sourceType;
    private Integer moduleCount;
    private Integer pageCount;
    private Integer apiCount;
    private Integer tableCount;
    private Integer permissionCount;
    private Integer documentCount;
    private String status;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getRepoId() { return repoId; }
    public void setRepoId(Long repoId) { this.repoId = repoId; }
    public String getRepoType() { return repoType; }
    public void setRepoType(String repoType) { this.repoType = repoType; }
    public String getRemoteUrl() { return remoteUrl; }
    public void setRemoteUrl(String remoteUrl) { this.remoteUrl = remoteUrl; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getCommitHash() { return commitHash; }
    public void setCommitHash(String commitHash) { this.commitHash = commitHash; }
    public String getIndexVersion() { return indexVersion; }
    public void setIndexVersion(String indexVersion) { this.indexVersion = indexVersion; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Integer getModuleCount() { return moduleCount; }
    public void setModuleCount(Integer moduleCount) { this.moduleCount = moduleCount; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    public Integer getApiCount() { return apiCount; }
    public void setApiCount(Integer apiCount) { this.apiCount = apiCount; }
    public Integer getTableCount() { return tableCount; }
    public void setTableCount(Integer tableCount) { this.tableCount = tableCount; }
    public Integer getPermissionCount() { return permissionCount; }
    public void setPermissionCount(Integer permissionCount) { this.permissionCount = permissionCount; }
    public Integer getDocumentCount() { return documentCount; }
    public void setDocumentCount(Integer documentCount) { this.documentCount = documentCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("batchId", getBatchId())
            .append("projectId", getProjectId())
            .append("repoId", getRepoId())
            .append("repoType", getRepoType())
            .append("remoteUrl", getRemoteUrl())
            .append("branchName", getBranchName())
            .append("commitHash", getCommitHash())
            .append("indexVersion", getIndexVersion())
            .append("sourceType", getSourceType())
            .append("moduleCount", getModuleCount())
            .append("pageCount", getPageCount())
            .append("apiCount", getApiCount())
            .append("tableCount", getTableCount())
            .append("permissionCount", getPermissionCount())
            .append("documentCount", getDocumentCount())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
