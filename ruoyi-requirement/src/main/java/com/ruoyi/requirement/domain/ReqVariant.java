package com.ruoyi.requirement.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 需求平台客户定制线对象 req_variant
 */
public class ReqVariant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 定制线ID */
    private Long variantId;

    /** 项目ID */
    private Long projectId;

    /** 定制线名称 */
    private String variantName;

    /** 定制线编码 */
    private String variantCode;

    /** 客户名称 */
    private String customerName;

    /** 范围类型 */
    private String scopeType;

    /** 基线分支 */
    private String baselineBranch;

    /** 分支策略 */
    private String branchPolicy;

    /** 说明 */
    private String description;

    /** 状态 */
    private String status;

    public Long getVariantId()
    {
        return variantId;
    }

    public void setVariantId(Long variantId)
    {
        this.variantId = variantId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public String getVariantName()
    {
        return variantName;
    }

    public void setVariantName(String variantName)
    {
        this.variantName = variantName;
    }

    public String getVariantCode()
    {
        return variantCode;
    }

    public void setVariantCode(String variantCode)
    {
        this.variantCode = variantCode;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getScopeType()
    {
        return scopeType;
    }

    public void setScopeType(String scopeType)
    {
        this.scopeType = scopeType;
    }

    public String getBaselineBranch()
    {
        return baselineBranch;
    }

    public void setBaselineBranch(String baselineBranch)
    {
        this.baselineBranch = baselineBranch;
    }

    public String getBranchPolicy()
    {
        return branchPolicy;
    }

    public void setBranchPolicy(String branchPolicy)
    {
        this.branchPolicy = branchPolicy;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("variantId", getVariantId())
            .append("projectId", getProjectId())
            .append("variantName", getVariantName())
            .append("variantCode", getVariantCode())
            .append("customerName", getCustomerName())
            .append("scopeType", getScopeType())
            .append("baselineBranch", getBaselineBranch())
            .append("branchPolicy", getBranchPolicy())
            .append("description", getDescription())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
