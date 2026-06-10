package com.ruoyi.requirement.dto;

import java.util.Map;
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

    private Map<String, Object> codexGlobalSkillPackage;

    private Map<String, Object> codexSetupPackage;

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

    public Map<String, Object> getCodexGlobalSkillPackage()
    {
        return codexGlobalSkillPackage;
    }

    public void setCodexGlobalSkillPackage(Map<String, Object> codexGlobalSkillPackage)
    {
        this.codexGlobalSkillPackage = codexGlobalSkillPackage;
    }

    public Map<String, Object> getCodexSetupPackage()
    {
        return codexSetupPackage;
    }

    public void setCodexSetupPackage(Map<String, Object> codexSetupPackage)
    {
        this.codexSetupPackage = codexSetupPackage;
    }
}
