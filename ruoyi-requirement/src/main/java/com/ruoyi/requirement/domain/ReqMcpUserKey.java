package com.ruoyi.requirement.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 人员MCP访问Key对象 req_mcp_user_key
 */
public class ReqMcpUserKey extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** Key ID */
    private Long keyId;

    /** 绑定用户ID */
    private Long userId;

    /** 用户账号 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    /** Key名称 */
    private String keyName;

    /** Key前缀 */
    private String keyPrefix;

    /** Key哈希 */
    @JsonIgnore
    private String keyHash;

    /** 状态（0正常 1停用） */
    private String status;

    /** 最近使用时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUsedTime;

    /** 最近使用IP */
    private String lastUsedIp;

    public Long getKeyId()
    {
        return keyId;
    }

    public void setKeyId(Long keyId)
    {
        this.keyId = keyId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getKeyName()
    {
        return keyName;
    }

    public void setKeyName(String keyName)
    {
        this.keyName = keyName;
    }

    public String getKeyPrefix()
    {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix)
    {
        this.keyPrefix = keyPrefix;
    }

    public String getKeyHash()
    {
        return keyHash;
    }

    public void setKeyHash(String keyHash)
    {
        this.keyHash = keyHash;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getLastUsedTime()
    {
        return lastUsedTime;
    }

    public void setLastUsedTime(Date lastUsedTime)
    {
        this.lastUsedTime = lastUsedTime;
    }

    public String getLastUsedIp()
    {
        return lastUsedIp;
    }

    public void setLastUsedIp(String lastUsedIp)
    {
        this.lastUsedIp = lastUsedIp;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("keyId", getKeyId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("nickName", getNickName())
            .append("keyName", getKeyName())
            .append("keyPrefix", getKeyPrefix())
            .append("status", getStatus())
            .append("lastUsedTime", getLastUsedTime())
            .append("lastUsedIp", getLastUsedIp())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
