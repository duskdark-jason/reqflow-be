package com.ruoyi.web.controller.requirement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.ruoyi.requirement.service.IReqMcpUserKeyService;
import com.ruoyi.requirement.service.impl.ReqMcpUserKeyServiceImpl;
import com.ruoyi.requirement.template.ReqflowCodexGlobalSkillTemplate;

/**
 * MCP人员Key管理Controller
 */
@RestController
@RequestMapping("/requirement/mcp/key")
public class ReqMcpKeyController extends BaseController
{
    @Autowired
    private IReqMcpUserKeyService reqMcpUserKeyService;

    @Value("${reqflow.mcp.public-url:}")
    private String mcpPublicUrl;

    @PreAuthorize("@ss.hasPermi('req:mcp:key:list')")
    @GetMapping("/list")
    public TableDataInfo list(ReqMcpUserKey reqMcpUserKey)
    {
        startPage();
        List<ReqMcpUserKey> list = reqMcpUserKeyService.selectReqMcpUserKeyList(reqMcpUserKey);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyPermi('req:mcp:key:list,req:mcp:key:add,req:mcp:key:edit')")
    @GetMapping("/config")
    public AjaxResult config(HttpServletRequest request)
    {
        String mcpAddress = mcpAddress(request);
        Map<String, Object> data = new HashMap<>();
        data.put("mcpAddress", mcpAddress);
        data.put("headerName", ReqMcpUserKeyServiceImpl.MCP_KEY_HEADER);
        data.put("codexConfigTemplate", codexConfigTemplate(mcpAddress));
        data.put("codexGlobalSkillPackage", ReqflowCodexGlobalSkillTemplate.globalSkillPackage());
        return success(data);
    }

    @PreAuthorize("@ss.hasAnyPermi('req:mcp:key:list,req:mcp:key:add,req:mcp:key:edit')")
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

    @PreAuthorize("@ss.hasPermi('req:mcp:key:add')")
    @Log(title = "MCP人员Key", businessType = BusinessType.INSERT, isSaveResponseData = false)
    @PostMapping
    public AjaxResult add(@RequestBody ReqMcpUserKey reqMcpUserKey, HttpServletRequest request)
    {
        return success(reqMcpUserKeyService.createKey(reqMcpUserKey, getUsername(), mcpAddress(request)));
    }

    @PreAuthorize("@ss.hasPermi('req:mcp:key:edit')")
    @Log(title = "MCP人员Key", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ReqMcpUserKey reqMcpUserKey)
    {
        reqMcpUserKey.setUpdateBy(getUsername());
        return toAjax(reqMcpUserKeyService.updateReqMcpUserKey(reqMcpUserKey));
    }

    @PreAuthorize("@ss.hasPermi('req:mcp:key:edit')")
    @Log(title = "MCP人员Key重置", businessType = BusinessType.UPDATE, isSaveResponseData = false)
    @PostMapping("/{keyId}/regenerate")
    public AjaxResult regenerate(@PathVariable("keyId") Long keyId, HttpServletRequest request)
    {
        return success(reqMcpUserKeyService.regenerateKey(keyId, getUsername(), mcpAddress(request)));
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
        String configuredMcpAddress = normalizeMcpPublicUrl();
        if (StringUtils.isNotEmpty(configuredMcpAddress))
        {
            return configuredMcpAddress;
        }

        String scheme = StringUtils.defaultIfEmpty(request.getHeader("X-Forwarded-Proto"), request.getScheme());
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
        return scheme + "://" + host + request.getContextPath() + "/requirement/mcp";
    }

    private String normalizeMcpPublicUrl()
    {
        String publicUrl = StringUtils.trim(mcpPublicUrl);
        while (StringUtils.isNotEmpty(publicUrl) && publicUrl.endsWith("/") && !publicUrl.endsWith("://"))
        {
            publicUrl = publicUrl.substring(0, publicUrl.length() - 1);
        }
        return publicUrl;
    }

    private boolean isDefaultPort(String scheme, int port)
    {
        return ("http".equalsIgnoreCase(scheme) && port == 80) || ("https".equalsIgnoreCase(scheme) && port == 443);
    }

    private String codexConfigTemplate(String mcpAddress)
    {
        return "{\n"
                + "  \"mcpServers\": {\n"
                + "    \"reqflow\": {\n"
                + "      \"url\": \"" + mcpAddress + "\",\n"
                + "      \"headers\": {\n"
                + "        \"" + ReqMcpUserKeyServiceImpl.MCP_KEY_HEADER + "\": \"创建或重置后返回的Key\"\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}";
    }
}
