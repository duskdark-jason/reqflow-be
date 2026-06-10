package com.ruoyi.requirement.dto;

import com.ruoyi.requirement.domain.ReqMcpUserKey;

/**
 * 人员MCP Key创建或重置后的一次性明文结果
 */
public class ReqMcpUserKeyCreateResult
{
    private ReqMcpUserKey key;

    private String plainKey;

    private String mcpAddress;

    private String headerName;

    private String codexConfig;

    public ReqMcpUserKey getKey()
    {
        return key;
    }

    public void setKey(ReqMcpUserKey key)
    {
        this.key = key;
    }

    public String getPlainKey()
    {
        return plainKey;
    }

    public void setPlainKey(String plainKey)
    {
        this.plainKey = plainKey;
    }

    public String getMcpAddress()
    {
        return mcpAddress;
    }

    public void setMcpAddress(String mcpAddress)
    {
        this.mcpAddress = mcpAddress;
    }

    public String getHeaderName()
    {
        return headerName;
    }

    public void setHeaderName(String headerName)
    {
        this.headerName = headerName;
    }

    public String getCodexConfig()
    {
        return codexConfig;
    }

    public void setCodexConfig(String codexConfig)
    {
        this.codexConfig = codexConfig;
    }
}
