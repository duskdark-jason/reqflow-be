package com.ruoyi.requirement.dto;

/**
 * MCP对外请求地址配置
 */
public class ReqMcpPublicHostConfig
{
    /** 系统参数键 */
    private String configKey;

    /** 对外访问host，格式为域名/IP加端口 */
    private String publicHost;

    /** 服务端拼接后的完整MCP地址 */
    private String mcpAddress;

    public String getConfigKey()
    {
        return configKey;
    }

    public void setConfigKey(String configKey)
    {
        this.configKey = configKey;
    }

    public String getPublicHost()
    {
        return publicHost;
    }

    public void setPublicHost(String publicHost)
    {
        this.publicHost = publicHost;
    }

    public String getMcpAddress()
    {
        return mcpAddress;
    }

    public void setMcpAddress(String mcpAddress)
    {
        this.mcpAddress = mcpAddress;
    }
}
