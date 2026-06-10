package com.ruoyi.requirement.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * MCP动作Token对象 req_action_token
 */
public class ReqActionToken extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** Token ID */
    private Long tokenId;

    /** 动作类型 */
    private String actionType;

    /** Token前缀 */
    private String tokenPrefix;

    /** Token哈希 */
    @JsonIgnore
    private String tokenHash;

    /** 目标MCP方法 */
    private String targetMethod;

    /** 项目ID */
    private Long projectId;

    /** 分支ID */
    private Long variantId;

    /** 需求ID */
    private Long demandId;

    /** 状态（0正常 1停用） */
    private String status;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /** 最近使用时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUsedTime;

    public Long getTokenId()
    {
        return tokenId;
    }

    public void setTokenId(Long tokenId)
    {
        this.tokenId = tokenId;
    }

    public String getActionType()
    {
        return actionType;
    }

    public void setActionType(String actionType)
    {
        this.actionType = actionType;
    }

    public String getTokenPrefix()
    {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix)
    {
        this.tokenPrefix = tokenPrefix;
    }

    public String getTokenHash()
    {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash)
    {
        this.tokenHash = tokenHash;
    }

    public String getTargetMethod()
    {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod)
    {
        this.targetMethod = targetMethod;
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

    public Long getDemandId()
    {
        return demandId;
    }

    public void setDemandId(Long demandId)
    {
        this.demandId = demandId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Date expireTime)
    {
        this.expireTime = expireTime;
    }

    public Date getLastUsedTime()
    {
        return lastUsedTime;
    }

    public void setLastUsedTime(Date lastUsedTime)
    {
        this.lastUsedTime = lastUsedTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("tokenId", getTokenId())
            .append("actionType", getActionType())
            .append("tokenPrefix", getTokenPrefix())
            .append("targetMethod", getTargetMethod())
            .append("projectId", getProjectId())
            .append("variantId", getVariantId())
            .append("demandId", getDemandId())
            .append("status", getStatus())
            .append("expireTime", getExpireTime())
            .append("lastUsedTime", getLastUsedTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
