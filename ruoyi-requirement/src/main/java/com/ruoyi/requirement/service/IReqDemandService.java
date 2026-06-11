package com.ruoyi.requirement.service;

import java.util.List;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.dto.ReqActionInstruction;

public interface IReqDemandService
{
    ReqDemand selectReqDemandByDemandId(Long demandId);
    List<ReqDemand> selectReqDemandList(ReqDemand reqDemand);
    int insertReqDemand(ReqDemand reqDemand);
    int updateReqDemand(ReqDemand reqDemand);
    int updateReqDemandStatus(Long demandId, String status, String updateBy);
    ReqActionInstruction createRequirementPlanInstruction(Long demandId, String operator);
    ReqActionInstruction createRequirementDevelopInstruction(Long demandId, String operator);
}
