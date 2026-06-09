package com.ruoyi.requirement.mapper;

import java.util.List;
import com.ruoyi.requirement.domain.ReqProject;

/**
 * 需求平台项目Mapper接口
 */
public interface ReqProjectMapper
{
    ReqProject selectReqProjectByProjectId(Long projectId);

    List<ReqProject> selectReqProjectList(ReqProject reqProject);

    int insertReqProject(ReqProject reqProject);

    int updateReqProject(ReqProject reqProject);

    int deleteReqProjectByProjectId(Long projectId);

    int deleteReqProjectByProjectIds(Long[] projectIds);

}
