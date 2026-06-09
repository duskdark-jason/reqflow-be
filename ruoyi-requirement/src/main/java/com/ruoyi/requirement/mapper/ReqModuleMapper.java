package com.ruoyi.requirement.mapper;

import java.util.List;
import com.ruoyi.requirement.domain.ReqModule;

/**
 * 需求平台模块功能点Mapper接口
 */
public interface ReqModuleMapper
{
    ReqModule selectReqModuleByModuleId(Long moduleId);

    List<ReqModule> selectReqModuleList(ReqModule reqModule);

    int insertReqModule(ReqModule reqModule);

    int updateReqModule(ReqModule reqModule);

    int deleteReqModuleByModuleId(Long moduleId);

    int deleteReqModuleByModuleIds(Long[] moduleIds);

}
