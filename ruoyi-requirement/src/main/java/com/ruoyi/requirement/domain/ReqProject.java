package com.ruoyi.requirement.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 需求平台项目对象 req_project
 */
public class ReqProject extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long projectId;

    /** 项目名称 */
    private String projectName;

    /** 项目编码 */
    private String projectCode;

    /** 项目说明 */
    private String description;

    /** 负责人用户ID */
    private Long ownerUserId;

    /** 工作空间AGENTS模板版本 */
    private String workspaceAgentsTemplateVersion;

    /** 状态 */
    private String status;

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Long getOwnerUserId()
    {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId)
    {
        this.ownerUserId = ownerUserId;
    }

    public String getWorkspaceAgentsTemplateVersion()
    {
        return workspaceAgentsTemplateVersion;
    }

    public void setWorkspaceAgentsTemplateVersion(String workspaceAgentsTemplateVersion)
    {
        this.workspaceAgentsTemplateVersion = workspaceAgentsTemplateVersion;
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
            .append("projectId", getProjectId())
            .append("projectName", getProjectName())
            .append("projectCode", getProjectCode())
            .append("description", getDescription())
            .append("ownerUserId", getOwnerUserId())
            .append("workspaceAgentsTemplateVersion", getWorkspaceAgentsTemplateVersion())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
