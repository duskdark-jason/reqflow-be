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
        assertTrue(ReqDemandStatusTransition.isAllowed("submitted", "plan_pending"));
        assertTrue(ReqDemandStatusTransition.isAllowed("plan_pending", "plan_ready"));
        assertTrue(ReqDemandStatusTransition.isAllowed("plan_ready", "confirmed"));
        assertTrue(ReqDemandStatusTransition.isAllowed("confirmed", "developing"));
        assertTrue(ReqDemandStatusTransition.isAllowed("developing", "review"));
        assertTrue(ReqDemandStatusTransition.isAllowed("review", "completed"));
        assertTrue(ReqDemandStatusTransition.isAllowed("completed", "archived"));
    }

    @Test
    void allowsLegacyIntermediateStatesForExistingDemand()
    {
        assertTrue(ReqDemandStatusTransition.isAllowed("review", "repairing"));
        assertTrue(ReqDemandStatusTransition.isAllowed("repairing", "review"));
    }

    @Test
    void allowsFeedbackAndSupplementFlow()
    {
        assertTrue(ReqDemandStatusTransition.isAllowed("submitted", "supplement_required"));
        assertTrue(ReqDemandStatusTransition.isAllowed("plan_pending", "supplement_required"));
        assertTrue(ReqDemandStatusTransition.isAllowed("submitted", "rejected"));
        assertTrue(ReqDemandStatusTransition.isAllowed("plan_pending", "rejected"));
        assertTrue(ReqDemandStatusTransition.isAllowed("supplement_required", "plan_pending"));
        assertTrue(ReqDemandStatusTransition.isAllowed("rejected", "archived"));
    }

    @Test
    void rejectsSkippedOrBackwardStatusFlow()
    {
        assertFalse(ReqDemandStatusTransition.isAllowed("draft", "completed"));
        assertFalse(ReqDemandStatusTransition.isAllowed("submitted", "plan_ready"));
        assertFalse(ReqDemandStatusTransition.isAllowed("developing", "submitted"));
        assertFalse(ReqDemandStatusTransition.isAllowed("archived", "submitted"));
    }
}
