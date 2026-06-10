package com.ruoyi.web.controller.requirement;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.requirement.mcp.McpRequest;
import com.ruoyi.requirement.mcp.McpResponse;
import com.ruoyi.requirement.mcp.McpService;
import com.ruoyi.requirement.service.IReqMcpUserKeyService;
import com.ruoyi.requirement.service.impl.ReqMcpUserKeyServiceImpl;

@RestController
@RequestMapping("/requirement/mcp")
public class ReqMcpController
{
    private static final String[] MCP_PERMISSIONS = new String[] {
        "req:package:save", "req:index:import", "req:project:query"
    };

    @Autowired
    private McpService mcpService;

    @Autowired
    private IReqMcpUserKeyService reqMcpUserKeyService;

    @PostMapping
    public ResponseEntity<McpResponse> handle(@RequestBody McpRequest request, HttpServletRequest httpRequest)
    {
        Authentication originalAuthentication = SecurityContextHolder.getContext().getAuthentication();
        boolean useMcpKey = false;
        try
        {
            if (request == null)
            {
                return ResponseEntity.ok(McpResponse.error(null, "MCP请求不能为空"));
            }
            String plainKey = httpRequest.getHeader(ReqMcpUserKeyServiceImpl.MCP_KEY_HEADER);
            if (StringUtils.isNotEmpty(plainKey))
            {
                LoginUser loginUser = reqMcpUserKeyService.authenticate(plainKey, IpUtils.getIpAddr(httpRequest));
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                useMcpKey = true;
            }
            if (!hasAnyMcpPermission())
            {
                return ResponseEntity.ok(McpResponse.error(request.getId(), "调用MCP需要权限：req:package:save、req:index:import 或 req:project:query"));
            }
            McpResponse response = mcpService.handle(request);
            if (isNotification(request))
            {
                return ResponseEntity.accepted().build();
            }
            return ResponseEntity.ok(response);
        }
        catch (Exception e)
        {
            return ResponseEntity.ok(McpResponse.error(request == null ? null : request.getId(), e.getMessage()));
        }
        finally
        {
            if (useMcpKey)
            {
                SecurityContextHolder.getContext().setAuthentication(originalAuthentication);
            }
        }
    }

    private boolean hasAnyMcpPermission()
    {
        for (String permission : MCP_PERMISSIONS)
        {
            try
            {
                if (SecurityUtils.hasPermi(permission))
                {
                    return true;
                }
            }
            catch (Exception e)
            {
                return false;
            }
        }
        return false;
    }

    private boolean isNotification(McpRequest request)
    {
        return request != null && request.getId() == null
                && request.getMethod() != null && request.getMethod().startsWith("notifications/");
    }
}
