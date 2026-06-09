package com.ruoyi.requirement.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 模块影响面索引对象 req_impact_item
 */
public class ReqImpactItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long impactId;
    private Long batchId;
    private Long projectId;
    private Long repoId;
    private Long variantId;
    private String branchName;
    private String moduleCode;
    private Long moduleId;
    private String itemType;
    private String itemName;
    private String itemKey;
    private String relativePath;
    private String httpMethod;
    private String apiPath;
    private String permissionKey;
    private String tableName;
    private String summary;
    private String tags;
    private String status;

    public Long getImpactId() { return impactId; }
    public void setImpactId(Long impactId) { this.impactId = impactId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getRepoId() { return repoId; }
    public void setRepoId(Long repoId) { this.repoId = repoId; }
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public Long getModuleId() { return moduleId; }
    public void setModuleId(Long moduleId) { this.moduleId = moduleId; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemKey() { return itemKey; }
    public void setItemKey(String itemKey) { this.itemKey = itemKey; }
    public String getRelativePath() { return relativePath; }
    public void setRelativePath(String relativePath) { this.relativePath = relativePath; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public String getApiPath() { return apiPath; }
    public void setApiPath(String apiPath) { this.apiPath = apiPath; }
    public String getPermissionKey() { return permissionKey; }
    public void setPermissionKey(String permissionKey) { this.permissionKey = permissionKey; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("impactId", getImpactId())
            .append("batchId", getBatchId())
            .append("projectId", getProjectId())
            .append("repoId", getRepoId())
            .append("variantId", getVariantId())
            .append("branchName", getBranchName())
            .append("moduleCode", getModuleCode())
            .append("moduleId", getModuleId())
            .append("itemType", getItemType())
            .append("itemName", getItemName())
            .append("itemKey", getItemKey())
            .append("relativePath", getRelativePath())
            .append("httpMethod", getHttpMethod())
            .append("apiPath", getApiPath())
            .append("permissionKey", getPermissionKey())
            .append("tableName", getTableName())
            .append("summary", getSummary())
            .append("tags", getTags())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
