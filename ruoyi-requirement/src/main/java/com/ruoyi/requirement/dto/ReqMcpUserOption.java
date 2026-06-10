package com.ruoyi.requirement.dto;

/**
 * MCP Key可绑定用户选项
 */
public class ReqMcpUserOption
{
    private Long userId;

    private String userName;

    private String nickName;

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
}
