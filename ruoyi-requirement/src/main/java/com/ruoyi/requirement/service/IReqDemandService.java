package com.ruoyi.requirement.service;

import java.util.List;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.dto.ReqActionInstruction;
import com.ruoyi.requirement.dto.ReqMcpUserOption;

public interface IReqDemandService
{
    ReqDemand selectReqDemandByDemandId(Long demandId);
    List<ReqDemand> selectReqDemandList(ReqDemand reqDemand);
    List<ReqMcpUserOption> selectDeveloperOptions(String userName);
    int insertReqDemand(ReqDemand reqDemand);
    int updateReqDemand(ReqDemand reqDemand);
    int deleteReqDemandByDemandIds(Long[] demandIds, String operator);
    int updateReqDemandStatus(Long demandId, String status, String updateBy);
    void validateDemandReadable(Long demandId);
    void validateDemandPackageWritable(Long demandId, String artifactType);
    ReqActionInstruction createRequirementPlanInstruction(Long demandId, String operator);
    ReqActionInstruction createRequirementDevelopInstruction(Long demandId, String operator);
}
