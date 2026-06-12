package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.requirement.domain.ReqMcpUserKey;
import com.ruoyi.requirement.dto.ReqMcpUserKeyCreateResult;
import com.ruoyi.requirement.dto.ReqMcpUserOption;
import com.ruoyi.requirement.mapper.ReqMcpUserKeyMapper;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.ISysUserService;

class ReqMcpUserKeyServiceImplTest
{
    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

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
        assertNoCreateResultAccessor("getMcpAddress");
        assertNoCreateResultAccessor("getHeaderName");
        assertNoCreateResultAccessor("getCodexConfig");
        assertNoCreateResultAccessor("getCodexGlobalSkillPackage");
        assertNotNull(result.getCodexSetupPackage());
        assertEquals("reqflow-codex-setup", result.getCodexSetupPackage().get("packageName"));
        assertEquals("global", result.getCodexSetupPackage().get("installScope"));
        assertInstallCommands(result.getCodexSetupPackage(), result.getPlainKey());
        assertPackageDoesNotContainPlainKey(result.getCodexSetupPackage(), result.getPlainKey());

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
    void rejectsDeletedUserWhenAuthenticatingKey()
    {
        ReqMcpUserKeyMapper mapper = mock(ReqMcpUserKeyMapper.class);
        ISysUserService userService = mock(ISysUserService.class);
        ReqMcpUserKeyServiceImpl service = newService(mapper, userService, mock(ISysMenuService.class));

        String plainKey = "reqflow_mcp_deleted_user_key";
        ReqMcpUserKey stored = new ReqMcpUserKey();
        stored.setKeyId(102L);
        stored.setUserId(14L);
        stored.setKeyHash(service.hashKeyForTest(plainKey));
        stored.setStatus("0");
        SysUser deleted = enabledUser(14L, "deleted_user");
        deleted.setDelFlag("2");
        when(mapper.selectReqMcpUserKeyByKeyHash(stored.getKeyHash())).thenReturn(stored);
        when(userService.selectUserById(14L)).thenReturn(deleted);

        assertThrows(ServiceException.class, () -> service.authenticate(plainKey, "127.0.0.1"));
        verify(mapper, never()).updateLastUsed(102L, "127.0.0.1");
    }

    @Test
    void nonAdminCreateKeyBindsCurrentUser()
    {
        ReqMcpUserKeyMapper mapper = mock(ReqMcpUserKeyMapper.class);
        ISysUserService userService = mock(ISysUserService.class);
        ReqMcpUserKeyServiceImpl service = newService(mapper, userService, mock(ISysMenuService.class));
        when(userService.selectUserById(12L)).thenReturn(enabledUser(12L, "developer"));
        mockLoginUser(12L, "requirement_developer");

        ReqMcpUserKey request = new ReqMcpUserKey();
        request.setUserId(13L);
        request.setKeyName("个人Codex");

        service.createKey(request, "developer", "http://localhost:8080/requirement/mcp");

        ArgumentCaptor<ReqMcpUserKey> captor = forClass(ReqMcpUserKey.class);
        verify(mapper).insertReqMcpUserKey(captor.capture());
        assertEquals(12L, captor.getValue().getUserId());
    }

    @Test
    void selectsOnlyEnabledNotDeletedUserOptions()
    {
        ISysUserService userService = mock(ISysUserService.class);
        ReqMcpUserKeyServiceImpl service = newService(mock(ReqMcpUserKeyMapper.class), userService, mock(ISysMenuService.class));
        SysUser enabled = enabledUser(12L, "developer");
        SysUser disabled = enabledUser(13L, "disabled");
        disabled.setStatus("1");
        SysUser deleted = enabledUser(14L, "deleted");
        deleted.setDelFlag("2");
        when(userService.selectUserList(any(SysUser.class))).thenReturn(Arrays.asList(enabled, disabled, deleted));

        List<ReqMcpUserOption> options = service.selectUserOptions("dev");

        assertEquals(1, options.size());
        assertEquals(12L, options.get(0).getUserId());
        assertEquals("developer", options.get(0).getUserName());
        ArgumentCaptor<SysUser> queryCaptor = forClass(SysUser.class);
        verify(userService).selectUserList(queryCaptor.capture());
        assertEquals("dev", queryCaptor.getValue().getUserName());
        assertEquals("0", queryCaptor.getValue().getStatus());
    }

    @Test
    void nonAdminUserOptionsOnlyReturnsCurrentUser()
    {
        ISysUserService userService = mock(ISysUserService.class);
        ReqMcpUserKeyServiceImpl service = newService(mock(ReqMcpUserKeyMapper.class), userService, mock(ISysMenuService.class));
        when(userService.selectUserById(12L)).thenReturn(enabledUser(12L, "developer"));
        mockLoginUser(12L, "requirement_developer");

        List<ReqMcpUserOption> options = service.selectUserOptions("other");

        assertEquals(1, options.size());
        assertEquals(12L, options.get(0).getUserId());
        verify(userService, never()).selectUserList(any(SysUser.class));
    }

    @Test
    void createsInstructionPackageWithoutRecoveringPlainKey()
    {
        ReqMcpUserKeyMapper mapper = mock(ReqMcpUserKeyMapper.class);
        ReqMcpUserKeyServiceImpl service = newService(mapper, mock(ISysUserService.class), mock(ISysMenuService.class));
        ReqMcpUserKey key = new ReqMcpUserKey();
        key.setKeyId(99L);
        key.setUserId(12L);
        key.setKeyName("个人Codex");
        key.setStatus("0");
        when(mapper.selectReqMcpUserKeyByKeyId(99L)).thenReturn(key);
        mockLoginUser(1L, "admin");

        ReqMcpUserKeyCreateResult result = service.createInstruction(99L, "http://localhost:8080/requirement/mcp");

        assertEquals(key, result.getKey());
        assertEquals(null, result.getPlainKey());
        assertEquals("reqflow-codex-setup", result.getCodexSetupPackage().get("packageName"));
    }

    @Test
    void rejectsOtherUserInstructionPackageForNonAdmin()
    {
        ReqMcpUserKeyMapper mapper = mock(ReqMcpUserKeyMapper.class);
        ReqMcpUserKeyServiceImpl service = newService(mapper, mock(ISysUserService.class), mock(ISysMenuService.class));
        ReqMcpUserKey key = new ReqMcpUserKey();
        key.setKeyId(99L);
        key.setUserId(13L);
        key.setKeyName("他人Codex");
        key.setStatus("0");
        when(mapper.selectReqMcpUserKeyByKeyId(99L)).thenReturn(key);
        mockLoginUser(12L, "requirement_developer");

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.createInstruction(99L, "http://localhost:8080/requirement/mcp"));

        assertTrue(exception.getMessage().contains("只能查看自己的MCP Key指令"));
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

    private void assertPackageDoesNotContainPlainKey(Map<String, Object> skillPackage, String plainKey)
    {
        String packageText = String.valueOf(skillPackage);
        assertTrue(packageText.contains("SKILL.md"), packageText);
        assertTrue(packageText.contains("name: \"reqflow-mcp\""), packageText);
        assertTrue(packageText.contains("mcp__reqflow.get_harness_template"), packageText);
        assertTrue(packageText.contains("mcp__reqflow.publish_repository_index"), packageText);
        assertTrue(packageText.contains("mcp__reqflow.register_harness_init_result"), packageText);
        assertFalse(packageText.contains(plainKey), packageText);
        assertFalse(packageText.contains("mkdir -p"), packageText);
        assertFalse(packageText.contains("$HOME/.codex"), packageText);
    }

    @SuppressWarnings("unchecked")
    private void assertInstallCommands(Map<String, Object> setupPackage, String plainKey)
    {
        Object commandsValue = setupPackage.get("installCommands");
        assertNotNull(commandsValue);
        List<Map<String, Object>> commands = (List<Map<String, Object>>) commandsValue;
        assertEquals(2, commands.size());

        Map<String, Object> bash = commands.stream()
                .filter(command -> "macos-linux".equals(command.get("platform")))
                .findFirst()
                .orElseThrow();
        assertEquals("bash", bash.get("language"));
        assertTrue(String.valueOf(bash.get("command")).contains("install.sh"));
        assertTrue(String.valueOf(bash.get("command")).contains("${REQFLOW_MCP_KEY}"));

        Map<String, Object> powershell = commands.stream()
                .filter(command -> "windows-powershell".equals(command.get("platform")))
                .findFirst()
                .orElseThrow();
        assertEquals("powershell", powershell.get("language"));
        assertTrue(String.valueOf(powershell.get("command")).contains("install.ps1"));
        assertTrue(String.valueOf(powershell.get("command")).contains("${REQFLOW_MCP_KEY}"));

        assertFalse(String.valueOf(commands).contains(plainKey), String.valueOf(commands));
        assertFalse(String.valueOf(commands).contains("actionToken"), String.valueOf(commands));
    }

    private void assertNoCreateResultAccessor(String methodName)
    {
        boolean exists = Arrays.stream(ReqMcpUserKeyCreateResult.class.getMethods())
                .map(Method::getName)
                .anyMatch(methodName::equals);
        assertFalse(exists, methodName + " should not be exposed");
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

    private void mockLoginUser(Long userId, String roleKey)
    {
        SysRole role = new SysRole();
        role.setRoleKey(roleKey);
        SysUser user = enabledUser(userId, roleKey);
        user.setRoles(List.of(role));
        LoginUser loginUser = new LoginUser(userId, 1L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities()));
    }
}
