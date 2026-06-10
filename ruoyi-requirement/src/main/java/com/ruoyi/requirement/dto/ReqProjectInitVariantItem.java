package com.ruoyi.requirement.dto;

import java.util.Date;

public class ReqProjectInitVariantItem
{
    private Long variantId;
    private Long projectId;
    private String branchLabel;
    private String variantName;
    private String variantCode;
    private String customerName;
    private String scopeType;
    private String baselineBranch;
    private String branchPolicy;
    private String mcpKey;
    private ReqActionInstruction initInstruction;
    private Integer totalModules;
    private Integer indexedModules;
    private Integer manualModules;
    private Integer indexedRepositoryCount;
    private Integer unindexedRepositoryCount;
    private Date latestIndexedAt;
    private String latestCommit;
    private String description;
    private String status;
    private String remark;

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getBranchLabel() { return branchLabel; }
    public void setBranchLabel(String branchLabel) { this.branchLabel = branchLabel; }
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public String getVariantCode() { return variantCode; }
    public void setVariantCode(String variantCode) { this.variantCode = variantCode; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public String getBaselineBranch() { return baselineBranch; }
    public void setBaselineBranch(String baselineBranch) { this.baselineBranch = baselineBranch; }
    public String getBranchPolicy() { return branchPolicy; }
    public void setBranchPolicy(String branchPolicy) { this.branchPolicy = branchPolicy; }
    public String getMcpKey() { return mcpKey; }
    public void setMcpKey(String mcpKey) { this.mcpKey = mcpKey; }
    public ReqActionInstruction getInitInstruction() { return initInstruction; }
    public void setInitInstruction(ReqActionInstruction initInstruction) { this.initInstruction = initInstruction; }
    public Integer getTotalModules() { return totalModules; }
    public void setTotalModules(Integer totalModules) { this.totalModules = totalModules; }
    public Integer getIndexedModules() { return indexedModules; }
    public void setIndexedModules(Integer indexedModules) { this.indexedModules = indexedModules; }
    public Integer getManualModules() { return manualModules; }
    public void setManualModules(Integer manualModules) { this.manualModules = manualModules; }
    public Integer getIndexedRepositoryCount() { return indexedRepositoryCount; }
    public void setIndexedRepositoryCount(Integer indexedRepositoryCount) { this.indexedRepositoryCount = indexedRepositoryCount; }
    public Integer getUnindexedRepositoryCount() { return unindexedRepositoryCount; }
    public void setUnindexedRepositoryCount(Integer unindexedRepositoryCount) { this.unindexedRepositoryCount = unindexedRepositoryCount; }
    public Date getLatestIndexedAt() { return latestIndexedAt; }
    public void setLatestIndexedAt(Date latestIndexedAt) { this.latestIndexedAt = latestIndexedAt; }
    public String getLatestCommit() { return latestCommit; }
    public void setLatestCommit(String latestCommit) { this.latestCommit = latestCommit; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
