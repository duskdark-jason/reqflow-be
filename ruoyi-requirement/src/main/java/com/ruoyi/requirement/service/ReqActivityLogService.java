package com.ruoyi.requirement.service;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.requirement.domain.ReqActivityLog;
import com.ruoyi.requirement.mapper.ReqActivityLogMapper;

@Service
public class ReqActivityLogService
{
    @Autowired
    private ReqActivityLogMapper reqActivityLogMapper;

    public void record(Long userId, Long projectId, Long demandId, String eventType, String clientType, String summary, String metadataJson)
    {
        ReqActivityLog log = new ReqActivityLog();
        log.setUserId(userId);
        log.setProjectId(projectId);
        log.setDemandId(demandId);
        log.setEventType(eventType);
        log.setClientType(clientType == null ? "web" : clientType);
        log.setSummary(summary);
        log.setMetadataJson(metadataJson);
        log.setEventTime(new Date());
        reqActivityLogMapper.insertReqActivityLog(log);
    }
}
