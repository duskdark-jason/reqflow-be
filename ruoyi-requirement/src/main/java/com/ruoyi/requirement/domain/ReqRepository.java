package com.ruoyi.requirement.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 需求平台代码仓库对象 req_repository
 */
public class ReqRepository extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 仓库ID */
    private Long repoId;

    /** 项目ID */
    private Long projectId;

    /** 仓库名称 */
    private String repoName;

    /** 仓库类型 */
    private String repoType;

    /** 仓库地址 */
    private String repoUrl;

    /** 本地路径提示 */
    private String localPathHint;

    /** 默认分支 */
    private String defaultBranch;

    /** Harness状态 */
    private String harnessStatus;

    /** Harness提交 */
    private String harnessCommit;

    /** 最近索引时间 */
    private Date lastIndexedAt;

    /** 状态 */
    private String status;

    public Long getRepoId()
    {
        return repoId;
    }

    public void setRepoId(Long repoId)
    {
        this.repoId = repoId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public String getRepoName()
    {
        return repoName;
    }

    public void setRepoName(String repoName)
    {
        this.repoName = repoName;
    }

    public String getRepoType()
    {
        return repoType;
    }

    public void setRepoType(String repoType)
    {
        this.repoType = repoType;
    }

    public String getRepoUrl()
    {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl)
    {
        this.repoUrl = repoUrl;
    }

    public String getLocalPathHint()
    {
        return localPathHint;
    }

    public void setLocalPathHint(String localPathHint)
    {
        this.localPathHint = localPathHint;
    }

    public String getDefaultBranch()
    {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch)
    {
        this.defaultBranch = defaultBranch;
    }

    public String getHarnessStatus()
    {
        return harnessStatus;
    }

    public void setHarnessStatus(String harnessStatus)
    {
        this.harnessStatus = harnessStatus;
    }

    public String getHarnessCommit()
    {
        return harnessCommit;
    }

    public void setHarnessCommit(String harnessCommit)
    {
        this.harnessCommit = harnessCommit;
    }

    public Date getLastIndexedAt()
    {
        return lastIndexedAt;
    }

    public void setLastIndexedAt(Date lastIndexedAt)
    {
        this.lastIndexedAt = lastIndexedAt;
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
            .append("repoId", getRepoId())
            .append("projectId", getProjectId())
            .append("repoName", getRepoName())
            .append("repoType", getRepoType())
            .append("repoUrl", getRepoUrl())
            .append("localPathHint", getLocalPathHint())
            .append("defaultBranch", getDefaultBranch())
            .append("harnessStatus", getHarnessStatus())
            .append("harnessCommit", getHarnessCommit())
            .append("lastIndexedAt", getLastIndexedAt())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
