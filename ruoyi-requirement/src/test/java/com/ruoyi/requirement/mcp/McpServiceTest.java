package com.ruoyi.requirement.mcp;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.dto.ReqIndexImportResult;
import com.ruoyi.requirement.dto.ReqRepositoryIndexImportRequest;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.service.IReqRepositoryIndexService;

class McpServiceTest
{
    @Test
    void toolListIncludesRepositoryIndexPublisher()
    {
        McpResponse response = new McpService().handle(request("tools/list", null));

        assertTrue(String.valueOf(response.getResult()).contains("publish_repository_index"));
    }

    @Test
    void publishRepositoryIndexToolDelegatesToIndexService()
    {
        IReqRepositoryIndexService indexService = mock(IReqRepositoryIndexService.class);
        ReqIndexImportResult importResult = new ReqIndexImportResult();
        importResult.setBatchId(9L);
        when(indexService.importRepositoryIndex(any(ReqRepositoryIndexImportRequest.class), eq("mcp"), any(), any())).thenReturn(importResult);

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "repositoryIndexService", indexService);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("projectId", 1L);
        arguments.put("repoId", 2L);
        arguments.put("repoType", "FRONTEND");
        arguments.put("remoteUrl", "git@example.com:reqflow-ui.git");
        arguments.put("branchName", "main");
        arguments.put("commitHash", "abc123");
        arguments.put("indexVersion", "v1");
        Map<String, Object> params = new HashMap<>();
        params.put("name", "publish_repository_index");
        params.put("arguments", arguments);

        service.handle(request("tools/call", params));

        verify(indexService).importRepositoryIndex(any(ReqRepositoryIndexImportRequest.class), eq("mcp"), any(), any());
    }

    @Test
    void publishRepositoryIndexToolRequiresIndexImportPermission()
    {
        IReqRepositoryIndexService indexService = mock(IReqRepositoryIndexService.class);
        McpService service = new TestableMcpService(false);
        ReflectionTestUtils.setField(service, "repositoryIndexService", indexService);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "publish_repository_index");
        params.put("arguments", new HashMap<>());

        McpResponse response = service.handle(request("tools/call", params));

        assertTrue(String.valueOf(response.getError()).contains("req:index:import"));
        verify(indexService, never()).importRepositoryIndex(any(), any(), any(), any());
    }

    @Test
    void registerHarnessInitResultRequiresPackageSavePermission()
    {
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        McpService service = new TestableMcpService(false);
        ReflectionTestUtils.setField(service, "reqRepositoryMapper", repositoryMapper);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("repoId", 2L);
        arguments.put("harnessStatus", "indexed");
        Map<String, Object> params = new HashMap<>();
        params.put("name", "register_harness_init_result");
        params.put("arguments", arguments);

        McpResponse response = service.handle(request("tools/call", params));

        assertTrue(String.valueOf(response.getError()).contains("req:package:save"));
        verify(repositoryMapper, never()).updateHarnessInitResult(any(ReqRepository.class));
    }

    private McpRequest request(String method, Map<String, Object> params)
    {
        McpRequest request = new McpRequest();
        request.setJsonrpc("2.0");
        request.setId(1);
        request.setMethod(method);
        request.setParams(params);
        return request;
    }

    private static class TestableMcpService extends McpService
    {
        private final boolean allowed;

        TestableMcpService(boolean allowed)
        {
            this.allowed = allowed;
        }

        @Override
        protected boolean hasPermission(String permission)
        {
            return allowed;
        }
    }
}
