package com.ruoyi.requirement.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.requirement.domain.ReqPackageVersion;

public interface ReqPackageVersionMapper
{
    List<ReqPackageVersion> selectReqPackageVersionListByDemandId(Long demandId);
    ReqPackageVersion selectLatestByDemandIdAndArtifactType(@Param("demandId") Long demandId, @Param("artifactType") String artifactType);
    Integer selectMaxVersionNo(@Param("demandId") Long demandId, @Param("artifactType") String artifactType);
    int insertReqPackageVersion(ReqPackageVersion reqPackageVersion);
    Long selectPackageCount();
    Long selectPackageCountByArtifactType(String artifactType);
}
