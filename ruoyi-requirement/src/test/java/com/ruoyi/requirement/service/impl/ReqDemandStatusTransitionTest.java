package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReqDemandStatusTransitionTest
{
    @Test
    void allowsPlannedStatusFlow()
    {
        assertTrue(ReqDemandStatusTransition.isAllowed("draft", "submitted"));
        assertTrue(ReqDemandStatusTransition.isAllowed("review", "repairing"));
        assertTrue(ReqDemandStatusTransition.isAllowed("repairing", "review"));
        assertTrue(ReqDemandStatusTransition.isAllowed("completed", "archived"));
    }

    @Test
    void rejectsSkippedOrBackwardStatusFlow()
    {
        assertFalse(ReqDemandStatusTransition.isAllowed("draft", "completed"));
        assertFalse(ReqDemandStatusTransition.isAllowed("developing", "submitted"));
        assertFalse(ReqDemandStatusTransition.isAllowed("archived", "submitted"));
    }
}
