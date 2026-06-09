package com.ruoyi.requirement.service;

import java.util.List;
import com.ruoyi.requirement.domain.ReqPackageVersion;

public interface IReqPackageService
{
    List<ReqPackageVersion> selectReqPackageVersionListByDemandId(Long demandId);
    ReqPackageVersion selectLatest(Long demandId, String artifactType);
    ReqPackageVersion saveVersion(Long demandId, String artifactType, String content, String versionNote);
    List<ReqPackageVersion> generateDraftPackage(Long demandId);
}
