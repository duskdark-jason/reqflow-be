package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.requirement.domain.ReqMcpUserKey;
import com.ruoyi.requirement.dto.ReqMcpUserKeyCreateResult;
import com.ruoyi.requirement.mapper.ReqMcpUserKeyMapper;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.ISysUserService;

class ReqMcpUserKeyServiceImplTest
{
    @Test
    void createsRandomKeyAndStoresOnlyHashAndPrefix()
    {
        ReqMcpUserKeyMapper mapper = mock(ReqMcpUserKeyMapper.class);
        ISysUserService userService = mock(ISysUserService.class);
        ReqMcpUserKeyServiceImpl service = newService(mapper, userService, mock(ISysMenuService.class));
        when(userService.selectUserById(12L)).thenReturn(enabledUser(12L, "developer"));

        ReqMcpUserKey request = new ReqMcpUserKey();
        request.setUserId(12L);
        request.setKeyName("开发人员Codex");

        ReqMcpUserKeyCreateResult result = service.createKey(request, "admin", "http://localhost:8080/requirement/mcp");

        assertNotNull(result.getPlainKey());
        assertTrue(result.getPlainKey().startsWith("reqflow_mcp_"));
        assertEquals("X-MCP-Key", result.getHeaderName());
        assertTrue(result.getCodexConfig().contains(result.getPlainKey()));

        ArgumentCaptor<ReqMcpUserKey> captor = forClass(ReqMcpUserKey.class);
        verify(mapper).insertReqMcpUserKey(captor.capture());
        ReqMcpUserKey saved = captor.getValue();
        assertEquals(12L, saved.getUserId());
        assertEquals("开发人员Codex", saved.getKeyName());
        assertEquals("admin", saved.getCreateBy());
        assertEquals("0", saved.getStatus());
        assertNotNull(saved.getKeyHash());
        assertNotEquals(result.getPlainKey(), saved.getKeyHash());
        assertTrue(result.getPlainKey().startsWith(saved.getKeyPrefix()));
    }

    @Test
    void rejectsDisabledUserWhenCreatingKey()
    {
        ReqMcpUserKeyMapper mapper = mock(ReqMcpUserKeyMapper.class);
        ISysUserService userService = mock(ISysUserService.class);
        ReqMcpUserKeyServiceImpl service = newService(mapper, userService, mock(ISysMenuService.class));
        SysUser disabled = enabledUser(13L, "requester");
        disabled.setStatus("1");
        when(userService.selectUserById(13L)).thenReturn(disabled);

        ReqMcpUserKey request = new ReqMcpUserKey();
        request.setUserId(13L);
        request.setKeyName("停用人员");

        assertThrows(ServiceException.class, () -> service.createKey(request, "admin", "http://localhost:8080/requirement/mcp"));
    }

    @Test
    void authenticatesEnabledKeyAsBoundUserPermissions()
    {
        ReqMcpUserKeyMapper mapper = mock(ReqMcpUserKeyMapper.class);
        ISysUserService userService = mock(ISysUserService.class);
        ISysMenuService menuService = mock(ISysMenuService.class);
        ReqMcpUserKeyServiceImpl service = newService(mapper, userService, menuService);

        String plainKey = "reqflow_mcp_test_key";
        ReqMcpUserKey stored = new ReqMcpUserKey();
        stored.setKeyId(99L);
        stored.setUserId(12L);
        stored.setKeyHash(service.hashKeyForTest(plainKey));
        stored.setStatus("0");
        when(mapper.selectReqMcpUserKeyByKeyHash(stored.getKeyHash())).thenReturn(stored);
        when(userService.selectUserById(12L)).thenReturn(enabledUser(12L, "developer"));
        when(menuService.selectMenuPermsByUserId(12L)).thenReturn(Collections.singleton("req:index:import"));

        LoginUser loginUser = service.authenticate(plainKey, "127.0.0.1");

        assertEquals(12L, loginUser.getUserId());
        assertTrue(loginUser.getPermissions().contains("req:index:import"));
        verify(mapper).updateLastUsed(99L, "127.0.0.1");
    }

    @Test
    void authenticatesAdminKeyWithAllPermissions()
    {
        ReqMcpUserKeyMapper mapper = mock(ReqMcpUserKeyMapper.class);
        ISysUserService userService = mock(ISysUserService.class);
        ReqMcpUserKeyServiceImpl service = newService(mapper, userService, mock(ISysMenuService.class));

        String plainKey = "reqflow_mcp_admin_key";
        ReqMcpUserKey stored = new ReqMcpUserKey();
        stored.setKeyId(100L);
        stored.setUserId(1L);
        stored.setKeyHash(service.hashKeyForTest(plainKey));
        stored.setStatus("0");
        when(mapper.selectReqMcpUserKeyByKeyHash(stored.getKeyHash())).thenReturn(stored);
        when(userService.selectUserById(1L)).thenReturn(enabledUser(1L, "admin"));

        LoginUser loginUser = service.authenticate(plainKey, "127.0.0.1");

        assertTrue(loginUser.getPermissions().contains(Constants.ALL_PERMISSION));
    }

    @Test
    void rejectsDisabledKey()
    {
        ReqMcpUserKeyMapper mapper = mock(ReqMcpUserKeyMapper.class);
        ReqMcpUserKeyServiceImpl service = newService(mapper, mock(ISysUserService.class), mock(ISysMenuService.class));

        String plainKey = "reqflow_mcp_disabled_key";
        ReqMcpUserKey stored = new ReqMcpUserKey();
        stored.setKeyId(101L);
        stored.setUserId(12L);
        stored.setKeyHash(service.hashKeyForTest(plainKey));
        stored.setStatus("1");
        when(mapper.selectReqMcpUserKeyByKeyHash(stored.getKeyHash())).thenReturn(stored);

        assertThrows(ServiceException.class, () -> service.authenticate(plainKey, "127.0.0.1"));
    }

    private ReqMcpUserKeyServiceImpl newService(ReqMcpUserKeyMapper mapper, ISysUserService userService, ISysMenuService menuService)
    {
        ReqMcpUserKeyServiceImpl service = new ReqMcpUserKeyServiceImpl();
        ReflectionTestUtils.setField(service, "mcpUserKeyMapper", mapper);
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "menuService", menuService);
        return service;
    }

    private SysUser enabledUser(Long userId, String userName)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setDeptId(100L);
        user.setUserName(userName);
        user.setNickName(userName);
        user.setStatus("0");
        user.setDelFlag("0");
        return user;
    }
}
