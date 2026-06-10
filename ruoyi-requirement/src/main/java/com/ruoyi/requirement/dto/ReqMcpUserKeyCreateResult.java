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

    public Map<String, Object> getCodexSetupPackage()
    {
        return codexSetupPackage;
    }

    public void setCodexSetupPackage(Map<String, Object> codexSetupPackage)
    {
        this.codexSetupPackage = codexSetupPackage;
    }
}
