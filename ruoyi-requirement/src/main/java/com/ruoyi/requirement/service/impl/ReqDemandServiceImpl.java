package com.ruoyi.requirement.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.mapper.ReqDemandMapper;
import com.ruoyi.requirement.service.IReqDemandService;
import com.ruoyi.requirement.service.ReqActivityLogService;

@Service
public class ReqDemandServiceImpl implements IReqDemandService
{
    @Autowired
    private ReqDemandMapper reqDemandMapper;

    @Autowired
    private ReqActivityLogService activityLogService;

    @Override
    public ReqDemand selectReqDemandByDemandId(Long demandId)
    {
        return reqDemandMapper.selectReqDemandByDemandId(demandId);
    }

    @Override
    public List<ReqDemand> selectReqDemandList(ReqDemand reqDemand)
    {
        return reqDemandMapper.selectReqDemandList(reqDemand);
    }

    @Override
    public int insertReqDemand(ReqDemand reqDemand)
    {
        reqDemand.setDemandNo(nextDemandNo());
        reqDemand.setStatus("submitted");
        if (reqDemand.getCreatorId() == null)
        {
            reqDemand.setCreatorId(currentUserId());
        }
        int rows = reqDemandMapper.insertReqDemand(reqDemand);
        if (rows > 0)
        {
            activityLogService.record(reqDemand.getCreatorId(), reqDemand.getProjectId(), reqDemand.getDemandId(),
                    "demand_submitted", "web", "提交需求：" + reqDemand.getTitle(), null);
        }
        return rows;
    }

    @Override
    public int updateReqDemand(ReqDemand reqDemand)
    {
        if (reqDemand.getDemandId() == null)
        {
            throw new ServiceException("需求ID不能为空");
        }
        ReqDemand current = reqDemandMapper.selectReqDemandByDemandId(reqDemand.getDemandId());
        if (current == null)
        {
            throw new ServiceException("需求不存在");
        }
        if (reqDemand.getStatus() != null && !reqDemand.getStatus().isBlank()
                && !reqDemand.getStatus().equals(current.getStatus())
                && !ReqDemandStatusTransition.isAllowed(current.getStatus(), reqDemand.getStatus()))
        {
            throw new ServiceException("需求状态流转不允许");
        }
        return reqDemandMapper.updateReqDemand(reqDemand);
    }

    @Override
    public int updateReqDemandStatus(Long demandId, String status, String updateBy)
    {
        ReqDemand current = reqDemandMapper.selectReqDemandByDemandId(demandId);
        if (current == null)
        {
            throw new ServiceException("需求不存在");
        }
        if (!ReqDemandStatusTransition.isAllowed(current.getStatus(), status))
        {
            throw new ServiceException("需求状态流转不允许");
        }
        int rows = reqDemandMapper.updateReqDemandStatus(demandId, status, updateBy);
        if (rows > 0 && "archived".equals(status))
        {
            activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                    "demand_archived", "web", "归档需求：" + current.getDemandNo(), null);
        }
        return rows;
    }

    private String nextDemandNo()
    {
        return "REQ-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + String.format("%03d", reqDemandMapper.selectTodayDemandCount() + 1);
    }

    private Long currentUserId()
    {
        try
        {
            return SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            return 0L;
        }
    }
}
