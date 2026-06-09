package com.ruoyi.requirement.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.requirement.domain.ReqActivityLog;

public interface ReqActivityLogMapper
{
    int insertReqActivityLog(ReqActivityLog reqActivityLog);
    Long selectActiveUserCount();
    List<Map<String, Object>> selectUserUsage();
}
