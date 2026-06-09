package com.ruoyi.requirement.mapper;

import java.util.List;
import com.ruoyi.requirement.domain.ReqIndexModule;

public interface ReqIndexModuleMapper
{
    List<ReqIndexModule> selectReqIndexModuleList(ReqIndexModule module);

    int insertReqIndexModule(ReqIndexModule module);
}
