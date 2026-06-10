package com.ruoyi.requirement.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 需求平台模块功能点对象 req_module
 */
public class ReqModule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模块ID */
    private Long moduleId;

    /** 项目ID */
    private Long projectId;

    /** 项目分支ID */
    private Long variantId;

    /** 上级模块ID */
    private Long parentId;

    /** 模块名称 */
    private String moduleName;

    /** 模块编码 */
    private String moduleCode;

    /** 模块类型 */
    private String moduleType;

    /** 仓库范围 */
    private String repoScope;

    /** 说明 */
    private String description;

    /** 排序 */
    private Integer orderNum;

    /** 状态 */
    private String status;

    public Long getModuleId()
    {
        return moduleId;
    }

    public void setModuleId(Long moduleId)
    {
        this.moduleId = moduleId;
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

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getModuleName()
    {
        return moduleName;
    }

    public void setModuleName(String moduleName)
    {
        this.moduleName = moduleName;
    }

    public String getModuleCode()
    {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode)
    {
        this.moduleCode = moduleCode;
    }

    public String getModuleType()
    {
        return moduleType;
    }

    public void setModuleType(String moduleType)
    {
        this.moduleType = moduleType;
    }

    public String getRepoScope()
    {
        return repoScope;
    }

    public void setRepoScope(String repoScope)
    {
        this.repoScope = repoScope;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
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
            .append("moduleId", getModuleId())
            .append("projectId", getProjectId())
            .append("variantId", getVariantId())
            .append("parentId", getParentId())
            .append("moduleName", getModuleName())
            .append("moduleCode", getModuleCode())
            .append("moduleType", getModuleType())
            .append("repoScope", getRepoScope())
            .append("description", getDescription())
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
