package com.ruoyi.requirement.service;

import com.ruoyi.requirement.dto.ReqProjectInitRequest;
import com.ruoyi.requirement.dto.ReqProjectInitResponse;

public interface IReqProjectInitService
{
    ReqProjectInitResponse selectProjectInit(Long projectId);

    ReqProjectInitResponse insertProjectInit(ReqProjectInitRequest request, String username);

    ReqProjectInitResponse updateProjectInit(ReqProjectInitRequest request, String username);
}
