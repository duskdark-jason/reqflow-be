package com.ruoyi.requirement.service;

import com.ruoyi.requirement.domain.ReqActionToken;
import com.ruoyi.requirement.domain.ReqProject;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.dto.ReqActionInstruction;

public interface IReqActionTokenService
{
    String ACTION_PROJECT_INIT = "project_init";
    String ACTION_REQUIREMENT_PLAN = "requirement_plan";
    String ACTION_REQUIREMENT_DEVELOP = "requirement_develop";

    String TARGET_REQUIREMENT_ANALYSIS = "requirement_analysis";
    String TARGET_REQUIREMENT_GENERATE = "requirement_generate";
    String TARGET_REQUIREMENT_DEVELOP = "requirement_develop";
    String TARGET_REQUIREMENT_REPAIR = "requirement_repair";

    ReqActionInstruction createProjectInitInstruction(ReqProject project, ReqVariant variant, String operator);

    ReqActionInstruction createInstruction(String actionType, Long projectId, Long variantId, Long demandId,
            String targetMethod, String prompt, String copyLabel, String operator);

    ReqActionToken resolveToken(String plainToken);
}
