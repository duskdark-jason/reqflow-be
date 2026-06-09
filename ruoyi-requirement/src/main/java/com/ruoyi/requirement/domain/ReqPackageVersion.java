package com.ruoyi.requirement.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 需求执行包版本对象 req_package_version
 */
public class ReqPackageVersion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 执行包ID */
    private Long packageId;

    /** 需求ID */
    private Long demandId;

    /** 产物类型 */
    private String artifactType;

    /** 版本号 */
    private Integer versionNo;

    /** 内容 */
    private String content;

    /** 状态 */
    private String status;

    /** 版本说明 */
    private String versionNote;

    public Long getPackageId()
    {
        return packageId;
    }

    public void setPackageId(Long packageId)
    {
        this.packageId = packageId;
    }

    public Long getDemandId()
    {
        return demandId;
    }

    public void setDemandId(Long demandId)
    {
        this.demandId = demandId;
    }

    public String getArtifactType()
    {
        return artifactType;
    }

    public void setArtifactType(String artifactType)
    {
        this.artifactType = artifactType;
    }

    public Integer getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo)
    {
        this.versionNo = versionNo;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getVersionNote()
    {
        return versionNote;
    }

    public void setVersionNote(String versionNote)
    {
        this.versionNote = versionNote;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("packageId", getPackageId())
            .append("demandId", getDemandId())
            .append("artifactType", getArtifactType())
            .append("versionNo", getVersionNo())
            .append("content", getContent())
            .append("status", getStatus())
            .append("versionNote", getVersionNote())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
}
