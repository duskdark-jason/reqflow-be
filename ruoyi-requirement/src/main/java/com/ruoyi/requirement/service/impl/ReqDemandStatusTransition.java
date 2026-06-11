package com.ruoyi.requirement.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 需求状态流转规则。
 */
public final class ReqDemandStatusTransition
{
    private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList(
            "draft->submitted",
            "submitted->plan_ready",
            "submitted->plan_pending",
            "plan_pending->plan_ready",
            "plan_ready->confirmed",
            "confirmed->developing",
            "developing->review",
            "review->repairing",
            "repairing->review",
            "review->completed",
            "completed->archived"));

    private ReqDemandStatusTransition()
    {
    }

    public static boolean isAllowed(String from, String to)
    {
        if (from == null || to == null || from.equals(to))
        {
            return false;
        }
        return ALLOWED.contains(from + "->" + to);
    }
}
