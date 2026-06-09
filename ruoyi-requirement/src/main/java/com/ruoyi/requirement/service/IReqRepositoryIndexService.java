package com.ruoyi.requirement.service;

import java.util.List;
import com.ruoyi.requirement.domain.ReqIndexModule;
import com.ruoyi.requirement.domain.ReqRepositoryIndexBatch;
import com.ruoyi.requirement.dto.ReqImpactSuggestQuery;
import com.ruoyi.requirement.dto.ReqImpactSuggestResult;
import com.ruoyi.requirement.dto.ReqIndexImportResult;
import com.ruoyi.requirement.dto.ReqRepositoryIndexImportRequest;

public interface IReqRepositoryIndexService
{
    ReqIndexImportResult importRepositoryIndex(ReqRepositoryIndexImportRequest request, String sourceType, String username, Long userId);

    List<ReqRepositoryIndexBatch> selectBatchList(ReqRepositoryIndexBatch batch);

    List<ReqIndexModule> selectModuleList(ReqIndexModule module);

    ReqImpactSuggestResult suggestImpact(ReqImpactSuggestQuery query);
}
