package com.ruoyi.requirement.dto;

public class ReqProjectInitVariantItem
{
    private Long variantId;
    private Long projectId;
    private String variantName;
    private String variantCode;
    private String customerName;
    private String scopeType;
    private String baselineBranch;
    private String branchPolicy;
    private String description;
    private String status;
    private String remark;

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
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
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
