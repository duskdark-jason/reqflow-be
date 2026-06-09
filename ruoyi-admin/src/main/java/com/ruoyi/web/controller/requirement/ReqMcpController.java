package com.ruoyi.web.controller.requirement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.requirement.mcp.McpRequest;
import com.ruoyi.requirement.mcp.McpResponse;
import com.ruoyi.requirement.mcp.McpService;

@RestController
@RequestMapping("/requirement/mcp")
public class ReqMcpController
{
    @Autowired
    private McpService mcpService;

    @PreAuthorize("@ss.hasAnyPermi('req:package:save,req:index:import')")
    @PostMapping
    public McpResponse handle(@RequestBody McpRequest request)
    {
        return mcpService.handle(request);
    }
}
