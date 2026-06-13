package com.ruoyi.requirement.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.requirement.domain.ReqMcpUserKey;
import com.ruoyi.requirement.dto.ReqMcpPublicHostConfig;
import com.ruoyi.requirement.service.IReqMcpUserKeyService;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.service.ISysConfigService;

/**
 * MCP人员Key管理Controller
 */
@RestController
@RequestMapping("/requirement/mcp/key")
public class ReqMcpKeyController extends BaseController
{
    private static final String MCP_PUBLIC_HOST_CONFIG_KEY = "reqflow.mcp.public-host";

    private static final String MCP_PUBLIC_HOST_CONFIG_NAME = "MCP服务对外访问地址";

    @Autowired
    private IReqMcpUserKeyService reqMcpUserKeyService;

    @Autowired
    private ISysConfigService configService;

    @PreAuthorize("@ss.hasPermi('req:mcp:key:list')")
    @GetMapping("/list")
    public TableDataInfo list(ReqMcpUserKey reqMcpUserKey)
    {
        startPage();
        List<ReqMcpUserKey> list = reqMcpUserKeyService.selectReqMcpUserKeyList(reqMcpUserKey);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyPermi('req:mcp:key:list,req:mcp:key:add')")
    @GetMapping("/user-options")
    public AjaxResult userOptions(String userName)
    {
        return success(reqMcpUserKeyService.selectUserOptions(userName));
    }

    @PreAuthorize("@ss.hasPermi('req:mcp:key:query')")
    @GetMapping(value = "/{keyId}")
    public AjaxResult getInfo(@PathVariable("keyId") Long keyId)
    {
        return success(reqMcpUserKeyService.selectReqMcpUserKeyByKeyId(keyId));
    }

    @PreAuthorize("@ss.hasPermi('req:mcp:key:query')")
    @GetMapping(value = "/{keyId}/instruction")
    public AjaxResult instruction(@PathVariable("keyId") Long keyId, HttpServletRequest request)
    {
        return success(reqMcpUserKeyService.createInstruction(keyId, mcpAddress(request)));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/config")
    public AjaxResult config(HttpServletRequest request)
    {
        ReqMcpPublicHostConfig config = new ReqMcpPublicHostConfig();
        config.setConfigKey(MCP_PUBLIC_HOST_CONFIG_KEY);
        config.setPublicHost(normalizeMcpPublicHost());
        config.setMcpAddress(mcpAddress(request));
        return success(config);
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @PutMapping("/config")
    public AjaxResult updateConfig(@RequestBody ReqMcpPublicHostConfig request, HttpServletRequest httpRequest)
    {
        request = request == null ? new ReqMcpPublicHostConfig() : request;
        String publicHost = validatePublicHost(request.getPublicHost());
        if (publicHost == null)
        {
            return error("MCP请求地址只填写域名/IP和端口，不要填写协议或路径");
        }
        if (StringUtils.isEmpty(publicHost))
        {
            return error("MCP请求地址不能为空");
        }

        if (!publicHost.equals(request.getPublicHost()))
        {
            request.setPublicHost(publicHost);
        }

        SysConfig existingConfig = loadPublicHostConfig();
        SysConfig config = new SysConfig();
        config.setConfigName(MCP_PUBLIC_HOST_CONFIG_NAME);
        config.setConfigKey(MCP_PUBLIC_HOST_CONFIG_KEY);
        config.setConfigValue(publicHost);
        config.setConfigType("Y");
        config.setRemark("MCP服务对外访问host:port，服务端自动拼接/requirement/mcp");

        int rows;
        if (existingConfig == null)
        {
            config.setCreateBy(getUsername());
            rows = configService.insertConfig(config);
        }
        else
        {
            config.setConfigId(existingConfig.getConfigId());
            config.setUpdateBy(getUsername());
            rows = configService.updateConfig(config);
        }
        return rows > 0 ? config(httpRequest) : error("保存MCP请求地址失败");
    }

    @PreAuthorize("@ss.hasPermi('req:mcp:key:add')")
    @Log(title = "MCP人员Key", businessType = BusinessType.INSERT, isSaveResponseData = false)
    @PostMapping
    public AjaxResult add(@RequestBody ReqMcpUserKey reqMcpUserKey, HttpServletRequest request)
    {
        return success(reqMcpUserKeyService.createKey(reqMcpUserKey, getUsername(), mcpAddress(request)));
    }

    @PreAuthorize("@ss.hasPermi('req:mcp:key:remove')")
    @Log(title = "MCP人员Key", businessType = BusinessType.DELETE)
    @DeleteMapping("/{keyIds}")
    public AjaxResult remove(@PathVariable Long[] keyIds)
    {
        return toAjax(reqMcpUserKeyService.deleteReqMcpUserKeyByKeyIds(keyIds));
    }

    private String mcpAddress(HttpServletRequest request)
    {
        String configuredMcpHost = normalizeMcpPublicHost();
        if (StringUtils.isNotEmpty(configuredMcpHost))
        {
            return requestScheme(request) + "://" + configuredMcpHost + contextPath(request) + "/requirement/mcp";
        }

        String scheme = requestScheme(request);
        String host = StringUtils.defaultIfEmpty(request.getHeader("X-Forwarded-Host"), request.getHeader("Host"));
        if (StringUtils.isEmpty(host))
        {
            host = request.getServerName();
            int port = request.getServerPort();
            if (port > 0 && !isDefaultPort(scheme, port))
            {
                host = host + ":" + port;
            }
        }
        return scheme + "://" + host + contextPath(request) + "/requirement/mcp";
    }

    private String normalizeMcpPublicHost()
    {
        String publicHost = configService == null ? null : StringUtils.trim(configService.selectConfigByKey(MCP_PUBLIC_HOST_CONFIG_KEY));
        if (StringUtils.isEmpty(publicHost))
        {
            return "";
        }
        publicHost = publicHost.replaceFirst("^https?://", "");
        int slashIndex = publicHost.indexOf('/');
        if (slashIndex >= 0)
        {
            publicHost = publicHost.substring(0, slashIndex);
        }
        while (publicHost.endsWith("/"))
        {
            publicHost = publicHost.substring(0, publicHost.length() - 1);
        }
        return publicHost;
    }

    private String validatePublicHost(String publicHost)
    {
        publicHost = StringUtils.trim(publicHost);
        if (StringUtils.isEmpty(publicHost))
        {
            return "";
        }
        String lowerPublicHost = publicHost.toLowerCase();
        if (lowerPublicHost.startsWith("http://") || lowerPublicHost.startsWith("https://")
                || publicHost.contains("/") || publicHost.contains("\\") || publicHost.contains("?")
                || publicHost.contains("#") || publicHost.matches(".*\\s+.*"))
        {
            return null;
        }
        return publicHost;
    }

    private SysConfig loadPublicHostConfig()
    {
        if (configService == null)
        {
            return null;
        }
        SysConfig query = new SysConfig();
        query.setConfigKey(MCP_PUBLIC_HOST_CONFIG_KEY);
        List<SysConfig> configs = configService.selectConfigList(query);
        if (configs == null)
        {
            return null;
        }
        return configs.stream()
                .filter(config -> MCP_PUBLIC_HOST_CONFIG_KEY.equals(config.getConfigKey()))
                .findFirst()
                .orElse(null);
    }

    private String contextPath(HttpServletRequest request)
    {
        String contextPath = request == null ? "" : request.getContextPath();
        return StringUtils.isEmpty(contextPath) || "/".equals(contextPath) ? "" : contextPath;
    }

    private String requestScheme(HttpServletRequest request)
    {
        if (request == null)
        {
            return "http";
        }
        return StringUtils.defaultIfEmpty(request.getHeader("X-Forwarded-Proto"), request.getScheme());
    }

    private boolean isDefaultPort(String scheme, int port)
    {
        return ("http".equalsIgnoreCase(scheme) && port == 80) || ("https".equalsIgnoreCase(scheme) && port == 443);
    }

}
