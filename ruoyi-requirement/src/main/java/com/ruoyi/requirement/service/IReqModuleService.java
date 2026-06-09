package com.ruoyi.requirement.service;

import java.util.List;
import com.ruoyi.requirement.domain.ReqModule;

/**
 * 需求平台模块功能点Service接口
 */
public interface IReqModuleService
{
    ReqModule selectReqModuleByModuleId(Long moduleId);
    List<ReqModule> selectReqModuleList(ReqModule reqModule);
    int insertReqModule(ReqModule reqModule);
    int updateReqModule(ReqModule reqModule);
    int deleteReqModuleByModuleIds(Long[] moduleIds);
    int deleteReqModuleByModuleId(Long moduleId);
}
