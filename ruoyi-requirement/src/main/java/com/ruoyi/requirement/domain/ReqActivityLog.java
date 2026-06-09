package com.ruoyi.requirement.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 需求平台业务事件对象 req_activity_log
 */
public class ReqActivityLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 事件ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 项目ID */
    private Long projectId;

    /** 需求ID */
    private Long demandId;

    /** 事件类型 */
    private String eventType;

    /** 客户端类型 */
    private String clientType;

    /** 摘要 */
    private String summary;

    /** 事件元数据 */
    private String metadataJson;

    /** 事件时间 */
    private Date eventTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public Long getDemandId()
    {
        return demandId;
    }

    public void setDemandId(Long demandId)
    {
        this.demandId = demandId;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public String getClientType()
    {
        return clientType;
    }

    public void setClientType(String clientType)
    {
        this.clientType = clientType;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getMetadataJson()
    {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson)
    {
        this.metadataJson = metadataJson;
    }

    public Date getEventTime()
    {
        return eventTime;
    }

    public void setEventTime(Date eventTime)
    {
        this.eventTime = eventTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("projectId", getProjectId())
            .append("demandId", getDemandId())
            .append("eventType", getEventType())
            .append("clientType", getClientType())
            .append("summary", getSummary())
            .append("metadataJson", getMetadataJson())
            .append("eventTime", getEventTime())
            .toString();
    }
}
