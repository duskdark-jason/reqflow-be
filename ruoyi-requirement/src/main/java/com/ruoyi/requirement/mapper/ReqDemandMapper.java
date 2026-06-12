package com.ruoyi.requirement.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.dto.ReqMcpUserOption;

public interface ReqDemandMapper
{
    ReqDemand selectReqDemandByDemandId(Long demandId);
    ReqDemand selectReqDemandByDemandNo(String demandNo);
    List<ReqDemand> selectReqDemandList(ReqDemand reqDemand);
    int insertReqDemand(ReqDemand reqDemand);
    int updateReqDemand(ReqDemand reqDemand);
    int deleteReqDemandByDemandIds(Long[] demandIds);
    int updateReqDemandStatus(@Param("demandId") Long demandId, @Param("status") String status, @Param("updateBy") String updateBy);
    int countEnabledUserByRoleKey(@Param("roleKey") String roleKey, @Param("userId") Long userId);
    List<ReqMcpUserOption> selectUserOptionsByRoleKey(@Param("roleKey") String roleKey, @Param("userName") String userName);
    Long selectDemandCount();
    List<Map<String, Object>> selectProjectRank();
}
