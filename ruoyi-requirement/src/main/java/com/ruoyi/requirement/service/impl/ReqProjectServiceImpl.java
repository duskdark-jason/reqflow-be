package com.ruoyi.requirement.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.requirement.domain.ReqProject;
import com.ruoyi.requirement.mapper.ReqProjectMapper;
import com.ruoyi.requirement.service.IReqProjectService;

/**
 * 需求平台项目Service业务层处理
 */
@Service
public class ReqProjectServiceImpl implements IReqProjectService
{
    @Autowired
    private ReqProjectMapper reqProjectMapper;

    @Override
    public ReqProject selectReqProjectByProjectId(Long projectId)
    {
        return reqProjectMapper.selectReqProjectByProjectId(projectId);
    }

    @Override
    public List<ReqProject> selectReqProjectList(ReqProject reqProject)
    {
        return reqProjectMapper.selectReqProjectList(reqProject);
    }

    @Override
    public int insertReqProject(ReqProject reqProject)
    {
        return reqProjectMapper.insertReqProject(reqProject);
    }

    @Override
    public int updateReqProject(ReqProject reqProject)
    {
        return reqProjectMapper.updateReqProject(reqProject);
    }

    @Override
    public int deleteReqProjectByProjectIds(Long[] projectIds)
    {
        return reqProjectMapper.deleteReqProjectByProjectIds(projectIds);
    }

    @Override
    public int deleteReqProjectByProjectId(Long projectId)
    {
        return reqProjectMapper.deleteReqProjectByProjectId(projectId);
    }
}
