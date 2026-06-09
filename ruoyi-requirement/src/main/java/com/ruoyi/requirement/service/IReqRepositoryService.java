package com.ruoyi.requirement.service;

import java.util.List;
import com.ruoyi.requirement.domain.ReqRepository;

/**
 * 需求平台代码仓库Service接口
 */
public interface IReqRepositoryService
{
    ReqRepository selectReqRepositoryByRepoId(Long repoId);
    List<ReqRepository> selectReqRepositoryList(ReqRepository reqRepository);
    int insertReqRepository(ReqRepository reqRepository);
    int updateReqRepository(ReqRepository reqRepository);
    int deleteReqRepositoryByRepoIds(Long[] repoIds);
    int deleteReqRepositoryByRepoId(Long repoId);
}
