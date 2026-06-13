package com.ruoyi.requirement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.requirement.template.ReqflowCodexGlobalSkillTemplate;
import com.ruoyi.requirement.template.ReqflowCodexInstallScriptTemplate;

/**
 * Codex 安装脚本Controller
 */
@RestController
@RequestMapping("/requirement/codex")
public class ReqCodexInstallController
{
    @GetMapping(value = "/install.sh", produces = "text/plain;charset=UTF-8")
    public String installShellScript()
    {
        return ReqflowCodexInstallScriptTemplate.shellScript();
    }

    @GetMapping(value = "/install.ps1", produces = "text/plain;charset=UTF-8")
    public String installPowerShellScript()
    {
        return ReqflowCodexInstallScriptTemplate.powerShellScript();
    }

    @GetMapping(value = "/skill/SKILL.md", produces = "text/markdown;charset=UTF-8")
    public String skillFile()
    {
        return ReqflowCodexGlobalSkillTemplate.skillContent();
    }
}
