package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.requirement.domain.ReqActionToken;
import com.ruoyi.requirement.domain.ReqProject;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.dto.ReqActionInstruction;
import com.ruoyi.requirement.mapper.ReqActionTokenMapper;
import com.ruoyi.requirement.service.IReqActionTokenService;

class ReqActionTokenServiceImplTest
{
    @Test
    void createsProjectInitInstructionWithUniqueTokenAndHashStorage()
    {
        ReqActionTokenMapper mapper = mock(ReqActionTokenMapper.class);
        ReqActionTokenServiceImpl service = newService(mapper);
        when(mapper.selectReqActionTokenByTokenHash(anyString())).thenReturn(null);

        ReqActionInstruction instruction = service.createProjectInitInstruction(project(), variant(), "admin");

        assertEquals(IReqActionTokenService.ACTION_PROJECT_INIT, instruction.getActionType());
        assertEquals("publish_repository_index", instruction.getTargetMethod());
        assertEquals("复制初始化指令", instruction.getCopyLabel());
        assertNotNull(instruction.getToken());
        assertTrue(instruction.getToken().startsWith("reqflow_action_"));
        assertTrue(instruction.getPrompt().contains("项目分支初始化"));
        assertTrue(instruction.getContent().contains(instruction.getPrompt()));
        assertTrue(instruction.getContent().contains(instruction.getToken()));
        assertTrue(instruction.getContent().contains("mcpServer: reqflow"));
        assertTrue(instruction.getContent().contains("toolName: publish_repository_index"));
        assertTrue(instruction.getContent().contains("mcpTool: reqflow.publish_repository_index"));
        assertTrue(instruction.getContent().contains("actionToken 是 publish_repository_index 的 arguments.actionToken"));
        assertTrue(instruction.getContent().contains("需求平台"));
        assertTrue(instruction.getContent().contains("main"));

        ArgumentCaptor<ReqActionToken> captor = forClass(ReqActionToken.class);
        verify(mapper).insertReqActionToken(captor.capture());
        ReqActionToken saved = captor.getValue();
        assertEquals(IReqActionTokenService.ACTION_PROJECT_INIT, saved.getActionType());
        assertEquals(10L, saved.getProjectId());
        assertEquals(31L, saved.getVariantId());
        assertEquals("publish_repository_index", saved.getTargetMethod());
        assertEquals("0", saved.getStatus());
        assertNotEquals(instruction.getToken(), saved.getTokenHash());
        assertEquals(service.hashTokenForTest(instruction.getToken()), saved.getTokenHash());
        assertTrue(instruction.getToken().startsWith(saved.getTokenPrefix()));
    }

    @Test
    void requirementAndDevelopInstructionsUseTheSameTokenShape()
    {
        ReqActionTokenMapper mapper = mock(ReqActionTokenMapper.class);
        ReqActionTokenServiceImpl service = newService(mapper);
        when(mapper.selectReqActionTokenByTokenHash(anyString())).thenReturn(null);

        ReqActionInstruction requirement = service.createInstruction(IReqActionTokenService.ACTION_REQUIREMENT_PLAN,
                10L, 31L, 200L, "create_requirement_package", "请读取需求上下文并生成需求包", "复制需求指令", "admin");
        ReqActionInstruction develop = service.createInstruction(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP,
                10L, 31L, 200L, "start_requirement_development", "请读取开发上下文并开始执行", "复制开发指令", "admin");

        assertTrue(requirement.getToken().startsWith("reqflow_action_"));
        assertTrue(develop.getToken().startsWith("reqflow_action_"));
        assertNotEquals(requirement.getToken(), develop.getToken());
        assertTrue(requirement.getContent().contains(requirement.getToken()));
        assertTrue(develop.getContent().contains(develop.getToken()));
        assertEquals(IReqActionTokenService.ACTION_REQUIREMENT_PLAN, requirement.getActionType());
        assertEquals(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP, develop.getActionType());
    }

    @Test
    void resolvesEnabledTokenAndUpdatesLastUsed()
    {
        ReqActionTokenMapper mapper = mock(ReqActionTokenMapper.class);
        ReqActionTokenServiceImpl service = newService(mapper);
        String plainToken = "reqflow_action_test_token";
        ReqActionToken stored = token(88L, "0");
        when(mapper.selectReqActionTokenByTokenHash(service.hashTokenForTest(plainToken))).thenReturn(stored);

        ReqActionToken resolved = service.resolveToken(plainToken);

        assertEquals(88L, resolved.getTokenId());
        verify(mapper).updateLastUsed(88L);
    }

    @Test
    void rejectsMissingOrDisabledToken()
    {
        ReqActionTokenMapper mapper = mock(ReqActionTokenMapper.class);
        ReqActionTokenServiceImpl service = newService(mapper);
        String plainToken = "reqflow_action_disabled_token";
        when(mapper.selectReqActionTokenByTokenHash(service.hashTokenForTest(plainToken))).thenReturn(token(89L, "1"));

        assertThrows(ServiceException.class, () -> service.resolveToken(""));
        assertThrows(ServiceException.class, () -> service.resolveToken(plainToken));
    }

    private ReqActionTokenServiceImpl newService(ReqActionTokenMapper mapper)
    {
        ReqActionTokenServiceImpl service = new ReqActionTokenServiceImpl();
        ReflectionTestUtils.setField(service, "actionTokenMapper", mapper);
        return service;
    }

    private ReqProject project()
    {
        ReqProject project = new ReqProject();
        project.setProjectId(10L);
        project.setProjectName("需求平台");
        project.setProjectCode("REQFLOW");
        return project;
    }

    private ReqVariant variant()
    {
        ReqVariant variant = new ReqVariant();
        variant.setVariantId(31L);
        variant.setProjectId(10L);
        variant.setVariantName("主线");
        variant.setVariantCode("MAIN");
        variant.setBaselineBranch("main");
        return variant;
    }

    private ReqActionToken token(Long tokenId, String status)
    {
        ReqActionToken token = new ReqActionToken();
        token.setTokenId(tokenId);
        token.setActionType(IReqActionTokenService.ACTION_PROJECT_INIT);
        token.setProjectId(10L);
        token.setVariantId(31L);
        token.setTargetMethod("publish_repository_index");
        token.setStatus(status);
        return token;
    }
}
