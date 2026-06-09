package com.ruoyi.requirement.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 仓库索引模块知识对象 req_index_module
 */
public class ReqIndexModule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long indexModuleId;
    private Long batchId;
    private Long projectId;
    private Long repoId;
    private Long variantId;
    private String parentCode;
    private String moduleCode;
    private String moduleName;
    private String moduleType;
    private String repoScope;
    private String relativePath;
    private String sourceRef;
    private String summary;
    private Integer orderNum;
    private String status;

    public Long getIndexModuleId() { return indexModuleId; }
    public void setIndexModuleId(Long indexModuleId) { this.indexModuleId = indexModuleId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getRepoId() { return repoId; }
    public void setRepoId(Long repoId) { this.repoId = repoId; }
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public String getParentCode() { return parentCode; }
    public void setParentCode(String parentCode) { this.parentCode = parentCode; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public String getModuleType() { return moduleType; }
    public void setModuleType(String moduleType) { this.moduleType = moduleType; }
    public String getRepoScope() { return repoScope; }
    public void setRepoScope(String repoScope) { this.repoScope = repoScope; }
    public String getRelativePath() { return relativePath; }
    public void setRelativePath(String relativePath) { this.relativePath = relativePath; }
    public String getSourceRef() { return sourceRef; }
    public void setSourceRef(String sourceRef) { this.sourceRef = sourceRef; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("indexModuleId", getIndexModuleId())
            .append("batchId", getBatchId())
            .append("projectId", getProjectId())
            .append("repoId", getRepoId())
            .append("variantId", getVariantId())
            .append("parentCode", getParentCode())
            .append("moduleCode", getModuleCode())
            .append("moduleName", getModuleName())
            .append("moduleType", getModuleType())
            .append("repoScope", getRepoScope())
            .append("relativePath", getRelativePath())
            .append("sourceRef", getSourceRef())
            .append("summary", getSummary())
            .append("orderNum", getOrderNum())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
