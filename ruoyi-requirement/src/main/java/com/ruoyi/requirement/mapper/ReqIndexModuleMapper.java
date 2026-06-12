package com.ruoyi.requirement.mapper;

import java.util.List;
import com.ruoyi.requirement.domain.ReqIndexModule;

public interface ReqIndexModuleMapper
{
    int checkReqIndexModuleTable();

    ReqIndexModule selectReqIndexModuleByIndexModuleId(Long indexModuleId);

    List<ReqIndexModule> selectReqIndexModuleList(ReqIndexModule module);

    int insertReqIndexModule(ReqIndexModule module);

    int deactivateReqIndexModulesByRepositoryBranch(ReqIndexModule module);
}
