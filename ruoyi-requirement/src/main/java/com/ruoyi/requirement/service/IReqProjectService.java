package com.ruoyi.requirement.service;

import java.util.List;
import com.ruoyi.requirement.domain.ReqProject;

/**
 * 需求平台项目Service接口
 */
public interface IReqProjectService
{
    ReqProject selectReqProjectByProjectId(Long projectId);
    List<ReqProject> selectReqProjectList(ReqProject reqProject);
    int insertReqProject(ReqProject reqProject);
    int updateReqProject(ReqProject reqProject);
    int deleteReqProjectByProjectIds(Long[] projectIds);
    int deleteReqProjectByProjectId(Long projectId);
}
