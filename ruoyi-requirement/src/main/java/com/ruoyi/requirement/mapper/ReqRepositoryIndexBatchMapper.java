package com.ruoyi.requirement.mapper;

import java.util.List;
import com.ruoyi.requirement.domain.ReqRepositoryIndexBatch;

public interface ReqRepositoryIndexBatchMapper
{
    int checkReqRepositoryIndexBatchTable();

    List<ReqRepositoryIndexBatch> selectReqRepositoryIndexBatchList(ReqRepositoryIndexBatch batch);

    int insertReqRepositoryIndexBatch(ReqRepositoryIndexBatch batch);
}
