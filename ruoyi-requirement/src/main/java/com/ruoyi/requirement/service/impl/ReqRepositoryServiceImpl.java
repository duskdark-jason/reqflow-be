package com.ruoyi.requirement.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.service.IReqRepositoryService;

/**
 * 需求平台代码仓库Service业务层处理
 */
@Service
public class ReqRepositoryServiceImpl implements IReqRepositoryService
{
    @Autowired
    private ReqRepositoryMapper reqRepositoryMapper;

    @Override
    public ReqRepository selectReqRepositoryByRepoId(Long repoId)
    {
        return reqRepositoryMapper.selectReqRepositoryByRepoId(repoId);
    }

    @Override
    public List<ReqRepository> selectReqRepositoryList(ReqRepository reqRepository)
    {
        return reqRepositoryMapper.selectReqRepositoryList(reqRepository);
    }

    @Override
    public int insertReqRepository(ReqRepository reqRepository)
    {
        return reqRepositoryMapper.insertReqRepository(reqRepository);
    }

    @Override
    public int updateReqRepository(ReqRepository reqRepository)
    {
        return reqRepositoryMapper.updateReqRepository(reqRepository);
    }

    @Override
    public int deleteReqRepositoryByRepoIds(Long[] repoIds)
    {
        return reqRepositoryMapper.deleteReqRepositoryByRepoIds(repoIds);
    }

    @Override
    public int deleteReqRepositoryByRepoId(Long repoId)
    {
        return reqRepositoryMapper.deleteReqRepositoryByRepoId(repoId);
    }
}
