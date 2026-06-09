package com.ruoyi.requirement.mapper;

import java.util.List;
import com.ruoyi.requirement.domain.ReqRepository;

/**
 * 需求平台代码仓库Mapper接口
 */
public interface ReqRepositoryMapper
{
    ReqRepository selectReqRepositoryByRepoId(Long repoId);

    List<ReqRepository> selectReqRepositoryList(ReqRepository reqRepository);

    int insertReqRepository(ReqRepository reqRepository);

    int updateReqRepository(ReqRepository reqRepository);

    int deleteReqRepositoryByRepoId(Long repoId);

    int deleteReqRepositoryByRepoIds(Long[] repoIds);

    int updateHarnessInitResult(ReqRepository reqRepository);

}
