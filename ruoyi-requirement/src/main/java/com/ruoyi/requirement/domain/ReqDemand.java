package com.ruoyi.requirement.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 需求对象 req_demand
 */
public class ReqDemand extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 需求ID */
    private Long demandId;

    /** 需求编号 */
    private String demandNo;

    /** 标题 */
    private String title;

    /** 需求类型 */
    private String demandType;

    /** 项目ID */
    private Long projectId;

    /** 定制线ID */
    private Long variantId;

    /** 模块ID */
    private Long moduleId;

    /** 功能点ID */
    private Long featureId;

    /** 业务背景 */
    private String businessBackground;

    /** 预期结果 */
    private String expectedResult;

    /** 影响页面 */
    private String impactPage;

    /** 影响接口 */
    private String impactApi;

    /** 影响数据 */
    private String impactData;

    /** 影响权限 */
    private String impactPermission;

    /** 导出或异步影响 */
    private String impactExportOrAsync;

    /** 验收标准 */
    private String acceptanceText;

    /** 状态 */
    private String status;

    /** 创建人ID */
    private Long creatorId;

    public Long getDemandId()
    {
        return demandId;
    }

    public void setDemandId(Long demandId)
    {
        this.demandId = demandId;
    }

    public String getDemandNo()
    {
        return demandNo;
    }

    public void setDemandNo(String demandNo)
    {
        this.demandNo = demandNo;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDemandType()
    {
        return demandType;
    }

    public void setDemandType(String demandType)
    {
        this.demandType = demandType;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public Long getVariantId()
    {
        return variantId;
    }

    public void setVariantId(Long variantId)
    {
        this.variantId = variantId;
    }

    public Long getModuleId()
    {
        return moduleId;
    }

    public void setModuleId(Long moduleId)
    {
        this.moduleId = moduleId;
    }

    public Long getFeatureId()
    {
        return featureId;
    }

    public void setFeatureId(Long featureId)
    {
        this.featureId = featureId;
    }

    public String getBusinessBackground()
    {
        return businessBackground;
    }

    public void setBusinessBackground(String businessBackground)
    {
        this.businessBackground = businessBackground;
    }

    public String getExpectedResult()
    {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult)
    {
        this.expectedResult = expectedResult;
    }

    public String getImpactPage()
    {
        return impactPage;
    }

    public void setImpactPage(String impactPage)
    {
        this.impactPage = impactPage;
    }

    public String getImpactApi()
    {
        return impactApi;
    }

    public void setImpactApi(String impactApi)
    {
        this.impactApi = impactApi;
    }

    public String getImpactData()
    {
        return impactData;
    }

    public void setImpactData(String impactData)
    {
        this.impactData = impactData;
    }

    public String getImpactPermission()
    {
        return impactPermission;
    }

    public void setImpactPermission(String impactPermission)
    {
        this.impactPermission = impactPermission;
    }

    public String getImpactExportOrAsync()
    {
        return impactExportOrAsync;
    }

    public void setImpactExportOrAsync(String impactExportOrAsync)
    {
        this.impactExportOrAsync = impactExportOrAsync;
    }

    public String getAcceptanceText()
    {
        return acceptanceText;
    }

    public void setAcceptanceText(String acceptanceText)
    {
        this.acceptanceText = acceptanceText;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getCreatorId()
    {
        return creatorId;
    }

    public void setCreatorId(Long creatorId)
    {
        this.creatorId = creatorId;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("demandId", getDemandId())
            .append("demandNo", getDemandNo())
            .append("title", getTitle())
            .append("demandType", getDemandType())
            .append("projectId", getProjectId())
            .append("variantId", getVariantId())
            .append("moduleId", getModuleId())
            .append("featureId", getFeatureId())
            .append("businessBackground", getBusinessBackground())
            .append("expectedResult", getExpectedResult())
            .append("impactPage", getImpactPage())
            .append("impactApi", getImpactApi())
            .append("impactData", getImpactData())
            .append("impactPermission", getImpactPermission())
            .append("impactExportOrAsync", getImpactExportOrAsync())
            .append("acceptanceText", getAcceptanceText())
            .append("status", getStatus())
            .append("creatorId", getCreatorId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
