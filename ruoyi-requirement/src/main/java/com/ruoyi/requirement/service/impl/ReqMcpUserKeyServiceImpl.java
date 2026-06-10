package com.ruoyi.requirement.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.requirement.domain.ReqMcpUserKey;
import com.ruoyi.requirement.dto.ReqMcpUserKeyCreateResult;
import com.ruoyi.requirement.dto.ReqMcpUserOption;
import com.ruoyi.requirement.mapper.ReqMcpUserKeyMapper;
import com.ruoyi.requirement.service.IReqMcpUserKeyService;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 人员MCP访问KeyService业务层处理
 */
@Service
public class ReqMcpUserKeyServiceImpl implements IReqMcpUserKeyService
{
    public static final String MCP_KEY_HEADER = "X-MCP-Key";

    private static final String KEY_PREFIX = "reqflow_mcp_";

    private static final int RANDOM_BYTES = 32;

    private static final int MAX_GENERATE_ATTEMPTS = 10;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private ReqMcpUserKeyMapper mcpUserKeyMapper;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysMenuService menuService;

    @Override
    public List<ReqMcpUserKey> selectReqMcpUserKeyList(ReqMcpUserKey reqMcpUserKey)
    {
        return mcpUserKeyMapper.selectReqMcpUserKeyList(reqMcpUserKey);
    }

    @Override
    public ReqMcpUserKey selectReqMcpUserKeyByKeyId(Long keyId)
    {
        return mcpUserKeyMapper.selectReqMcpUserKeyByKeyId(keyId);
    }

    @Override
    public List<ReqMcpUserOption> selectUserOptions(String userName)
    {
        SysUser query = new SysUser();
        query.setUserName(userName);
        query.setStatus(UserConstants.NORMAL);
        List<SysUser> users = userService.selectUserList(query);
        if (users == null)
        {
            return Collections.emptyList();
        }
        return users.stream()
                .filter(this::isEnabledUser)
                .map(this::toUserOption)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReqMcpUserKeyCreateResult createKey(ReqMcpUserKey reqMcpUserKey, String operator, String mcpAddress)
    {
        if (reqMcpUserKey == null)
        {
            throw new ServiceException("Key信息不能为空");
        }
        validateUser(reqMcpUserKey == null ? null : reqMcpUserKey.getUserId());
        if (StringUtils.isEmpty(reqMcpUserKey.getKeyName()))
        {
            throw new ServiceException("Key名称不能为空");
        }

        GeneratedKey generatedKey = generateUniqueKey();
        reqMcpUserKey.setKeyPrefix(prefixOf(generatedKey.plainKey));
        reqMcpUserKey.setKeyHash(generatedKey.hash);
        reqMcpUserKey.setStatus(StringUtils.defaultIfEmpty(reqMcpUserKey.getStatus(), UserConstants.NORMAL));
        reqMcpUserKey.setCreateBy(operator);
        mcpUserKeyMapper.insertReqMcpUserKey(reqMcpUserKey);
        return buildCreateResult(reqMcpUserKey, generatedKey.plainKey, mcpAddress);
    }

    @Override
    public int updateReqMcpUserKey(ReqMcpUserKey reqMcpUserKey)
    {
        if (reqMcpUserKey == null || reqMcpUserKey.getKeyId() == null)
        {
            throw new ServiceException("Key ID不能为空");
        }
        if (StringUtils.isEmpty(reqMcpUserKey.getKeyName()))
        {
            throw new ServiceException("Key名称不能为空");
        }
        ReqMcpUserKey exists = mcpUserKeyMapper.selectReqMcpUserKeyByKeyId(reqMcpUserKey.getKeyId());
        if (exists == null)
        {
            throw new ServiceException("MCP Key不存在");
        }
        if (reqMcpUserKey.getUserId() != null && !Objects.equals(reqMcpUserKey.getUserId(), exists.getUserId()))
        {
            throw new ServiceException("MCP Key不允许换绑用户");
        }
        reqMcpUserKey.setUserId(null);
        return mcpUserKeyMapper.updateReqMcpUserKey(reqMcpUserKey);
    }

    @Override
    public int deleteReqMcpUserKeyByKeyIds(Long[] keyIds)
    {
        return mcpUserKeyMapper.deleteReqMcpUserKeyByKeyIds(keyIds);
    }

    @Override
    @Transactional
    public ReqMcpUserKeyCreateResult regenerateKey(Long keyId, String operator, String mcpAddress)
    {
        ReqMcpUserKey exists = mcpUserKeyMapper.selectReqMcpUserKeyByKeyId(keyId);
        if (exists == null)
        {
            throw new ServiceException("MCP Key不存在");
        }
        validateUser(exists.getUserId());
        GeneratedKey generatedKey = generateUniqueKey();
        ReqMcpUserKey update = new ReqMcpUserKey();
        update.setKeyId(keyId);
        update.setKeyName(exists.getKeyName());
        update.setKeyPrefix(prefixOf(generatedKey.plainKey));
        update.setKeyHash(generatedKey.hash);
        update.setStatus(UserConstants.NORMAL);
        update.setUpdateBy(operator);
        mcpUserKeyMapper.updateReqMcpUserKey(update);
        exists.setKeyPrefix(update.getKeyPrefix());
        exists.setStatus(update.getStatus());
        return buildCreateResult(exists, generatedKey.plainKey, mcpAddress);
    }

    @Override
    public LoginUser authenticate(String plainKey, String ip)
    {
        if (StringUtils.isEmpty(plainKey))
        {
            throw new ServiceException("MCP Key不能为空");
        }
        ReqMcpUserKey stored = mcpUserKeyMapper.selectReqMcpUserKeyByKeyHash(hashKey(plainKey));
        if (stored == null || !UserConstants.NORMAL.equals(stored.getStatus()))
        {
            throw new ServiceException("MCP Key不存在或已停用");
        }
        SysUser user = validateUser(stored.getUserId());
        Set<String> permissions = permissionsForUser(user);
        LoginUser loginUser = new LoginUser(user.getUserId(), user.getDeptId(), user, permissions);
        loginUser.setToken("mcp-key-" + stored.getKeyId());
        mcpUserKeyMapper.updateLastUsed(stored.getKeyId(), ip);
        return loginUser;
    }

    String hashKeyForTest(String plainKey)
    {
        return hashKey(plainKey);
    }

    private SysUser validateUser(Long userId)
    {
        if (userId == null)
        {
            throw new ServiceException("绑定用户不能为空");
        }
        SysUser user = userService.selectUserById(userId);
        if (!isEnabledUser(user))
        {
            throw new ServiceException("绑定用户不存在或已停用");
        }
        return user;
    }

    private boolean isEnabledUser(SysUser user)
    {
        return user != null
                && UserConstants.NORMAL.equals(user.getStatus())
                && UserConstants.NORMAL.equals(user.getDelFlag());
    }

    private ReqMcpUserOption toUserOption(SysUser user)
    {
        ReqMcpUserOption option = new ReqMcpUserOption();
        option.setUserId(user.getUserId());
        option.setUserName(user.getUserName());
        option.setNickName(user.getNickName());
        return option;
    }

    private Set<String> permissionsForUser(SysUser user)
    {
        if (user.isAdmin())
        {
            return Collections.singleton(Constants.ALL_PERMISSION);
        }
        Set<String> permissions = menuService.selectMenuPermsByUserId(user.getUserId());
        return permissions == null ? Collections.emptySet() : new LinkedHashSet<>(permissions);
    }

    private GeneratedKey generateUniqueKey()
    {
        for (int i = 0; i < MAX_GENERATE_ATTEMPTS; i++)
        {
            String plainKey = randomPlainKey();
            String hash = hashKey(plainKey);
            if (mcpUserKeyMapper.selectReqMcpUserKeyByKeyHash(hash) == null)
            {
                return new GeneratedKey(plainKey, hash);
            }
        }
        throw new ServiceException("生成唯一MCP Key失败，请重试");
    }

    private String randomPlainKey()
    {
        byte[] bytes = new byte[RANDOM_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String prefixOf(String plainKey)
    {
        return plainKey.substring(0, Math.min(16, plainKey.length()));
    }

    private String hashKey(String plainKey)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(plainKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hashed)
            {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new ServiceException("MCP Key哈希失败");
        }
    }

    private ReqMcpUserKeyCreateResult buildCreateResult(ReqMcpUserKey key, String plainKey, String mcpAddress)
    {
        ReqMcpUserKeyCreateResult result = new ReqMcpUserKeyCreateResult();
        result.setKey(key);
        result.setPlainKey(plainKey);
        result.setMcpAddress(mcpAddress);
        result.setHeaderName(MCP_KEY_HEADER);
        result.setCodexConfig(codexConfig(mcpAddress, plainKey));
        return result;
    }

    private String codexConfig(String mcpAddress, String plainKey)
    {
        return "{\n"
                + "  \"mcpServers\": {\n"
                + "    \"reqflow\": {\n"
                + "      \"url\": \"" + escapeJson(mcpAddress) + "\",\n"
                + "      \"headers\": {\n"
                + "        \"" + MCP_KEY_HEADER + "\": \"" + escapeJson(plainKey) + "\"\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}";
    }

    private String escapeJson(String value)
    {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static class GeneratedKey
    {
        private final String plainKey;

        private final String hash;

        private GeneratedKey(String plainKey, String hash)
        {
            this.plainKey = plainKey;
            this.hash = hash;
        }
    }
}
