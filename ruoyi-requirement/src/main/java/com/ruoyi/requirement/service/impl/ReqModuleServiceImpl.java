package com.ruoyi.requirement.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.requirement.domain.ReqModule;
import com.ruoyi.requirement.mapper.ReqModuleMapper;
import com.ruoyi.requirement.service.IReqModuleService;

/**
 * 需求平台模块功能点Service业务层处理
 */
@Service
public class ReqModuleServiceImpl implements IReqModuleService
{
    @Autowired
    private ReqModuleMapper reqModuleMapper;

    @Override
    public ReqModule selectReqModuleByModuleId(Long moduleId)
    {
        return reqModuleMapper.selectReqModuleByModuleId(moduleId);
    }

    @Override
    public List<ReqModule> selectReqModuleList(ReqModule reqModule)
    {
        return reqModuleMapper.selectReqModuleList(reqModule);
    }

    @Override
    public int insertReqModule(ReqModule reqModule)
    {
        return reqModuleMapper.insertReqModule(reqModule);
    }

    @Override
    public int updateReqModule(ReqModule reqModule)
    {
        return reqModuleMapper.updateReqModule(reqModule);
    }

    @Override
    public int deleteReqModuleByModuleIds(Long[] moduleIds)
    {
        return reqModuleMapper.deleteReqModuleByModuleIds(moduleIds);
    }

    @Override
    public int deleteReqModuleByModuleId(Long moduleId)
    {
        return reqModuleMapper.deleteReqModuleByModuleId(moduleId);
    }
}
