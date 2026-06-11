package com.ruoyi.requirement.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.requirement.domain.ReqActionToken;
import com.ruoyi.requirement.domain.ReqProject;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.dto.ReqActionInstruction;
import com.ruoyi.requirement.mapper.ReqActionTokenMapper;
import com.ruoyi.requirement.service.IReqActionTokenService;

@Service
public class ReqActionTokenServiceImpl implements IReqActionTokenService
{
    private static final String TOKEN_PREFIX = "reqflow_action_";

    private static final String PROJECT_INIT_MCP_SERVER = "reqflow";

    private static final String PROJECT_INIT_TOOL_NAME = "publish_repository_index";

    private static final int RANDOM_BYTES = 32;

    private static final int MAX_GENERATE_ATTEMPTS = 10;

    private static final long ACTION_TOKEN_TTL_MILLIS = 24L * 60 * 60 * 1000;

    private static final String ACTION_TOKEN_USAGE_RULE = "有效期：24小时内有效，仅可使用一次；过期或已使用后需重新生成。";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private ReqActionTokenMapper actionTokenMapper;

    @Override
    @Transactional
    public ReqActionInstruction createProjectInitInstruction(ReqProject project, ReqVariant variant, String operator)
    {
        if (project == null || project.getProjectId() == null || variant == null || variant.getVariantId() == null)
        {
            throw new ServiceException("项目和分支不能为空");
        }
        String prompt = "请执行项目分支初始化，调用 reqflow MCP server 的 publish_repository_index tool 发布当前仓库索引。";
        ReqActionInstruction instruction = createInstruction(ACTION_PROJECT_INIT, project.getProjectId(), variant.getVariantId(),
                null, PROJECT_INIT_TOOL_NAME, prompt, "复制初始化指令", operator);
        // 项目初始化指令必须绑定项目分支，后续 publish_repository_index 才能自动校验远端和分支归属。
        instruction.setContent(projectInitInstructionContent(prompt, instruction.getTargetMethod(), instruction.getToken(),
                        project.getProjectId(), variant.getVariantId())
                + "\n项目：" + firstNotEmpty(project.getProjectName(), project.getProjectCode())
                + "\n分支：" + firstNotEmpty(variant.getVariantName(), variant.getVariantCode())
                + "\n真实分支：" + firstNotEmpty(variant.getBaselineBranch(), "未填写"));
        return instruction;
    }

    @Override
    @Transactional
    public ReqActionInstruction createInstruction(String actionType, Long projectId, Long variantId, Long demandId,
            String targetMethod, String prompt, String copyLabel, String operator)
    {
        validateInstruction(actionType, projectId, targetMethod, prompt);
        GeneratedToken generatedToken = generateUniqueToken();
        ReqActionToken token = new ReqActionToken();
        token.setActionType(actionType);
        token.setTokenPrefix(prefixOf(generatedToken.plainToken));
        token.setTokenHash(generatedToken.hash);
        token.setTargetMethod(targetMethod);
        token.setProjectId(projectId);
        token.setVariantId(variantId);
        token.setDemandId(demandId);
        token.setStatus(UserConstants.NORMAL);
        token.setExpireTime(new Date(System.currentTimeMillis() + ACTION_TOKEN_TTL_MILLIS));
        token.setCreateBy(operator);
        actionTokenMapper.insertReqActionToken(token);

        ReqActionInstruction instruction = new ReqActionInstruction();
        instruction.setActionType(actionType);
        instruction.setTargetMethod(targetMethod);
        instruction.setToken(generatedToken.plainToken);
        instruction.setTokenPrefix(token.getTokenPrefix());
        instruction.setPrompt(prompt);
        instruction.setCopyLabel(StringUtils.defaultIfEmpty(copyLabel, "复制指令"));
        instruction.setExpireTime(token.getExpireTime());
        instruction.setContent(prompt + "\ntargetMethod: " + targetMethod + "\nactionToken: " + generatedToken.plainToken
                + "\n" + ACTION_TOKEN_USAGE_RULE);
        return instruction;
    }

    @Override
    @Transactional
    public ReqActionToken resolveToken(String plainToken)
    {
        if (StringUtils.isEmpty(plainToken))
        {
            throw new ServiceException("动作Token不能为空");
        }
        ReqActionToken token = actionTokenMapper.selectReqActionTokenByTokenHash(hashToken(plainToken));
        if (token == null || !UserConstants.NORMAL.equals(token.getStatus()))
        {
            throw new ServiceException("动作Token不存在或已停用");
        }
        if (isExpired(token))
        {
            throw new ServiceException("动作Token已过期，请重新生成");
        }
        if (isUsed(token))
        {
            throw new ServiceException("动作Token已使用，请重新生成");
        }
        // 解析成功后记录使用时间，并通过条件更新保证并发场景下同一 Token 只能消费一次。
        if (token.getTokenId() != null)
        {
            int updated = actionTokenMapper.updateLastUsed(token.getTokenId());
            if (updated <= 0)
            {
                throw new ServiceException("动作Token已使用，请重新生成");
            }
        }
        return token;
    }

    String hashTokenForTest(String plainToken)
    {
        return hashToken(plainToken);
    }

    private void validateInstruction(String actionType, Long projectId, String targetMethod, String prompt)
    {
        if (StringUtils.isEmpty(actionType))
        {
            throw new ServiceException("动作类型不能为空");
        }
        if (projectId == null)
        {
            throw new ServiceException("项目不能为空");
        }
        if (StringUtils.isEmpty(targetMethod))
        {
            throw new ServiceException("目标接口不能为空");
        }
        if (StringUtils.isEmpty(prompt))
        {
            throw new ServiceException("初始化提示词不能为空");
        }
    }

    private GeneratedToken generateUniqueToken()
    {
        for (int i = 0; i < MAX_GENERATE_ATTEMPTS; i++)
        {
            String plainToken = randomPlainToken();
            String hash = hashToken(plainToken);
            if (actionTokenMapper.selectReqActionTokenByTokenHash(hash) == null)
            {
                // 动作 Token 与 MCP Key 一样只保存哈希，明文只出现在复制指令中。
                return new GeneratedToken(plainToken, hash);
            }
        }
        throw new ServiceException("生成唯一动作Token失败，请重试");
    }

    private String randomPlainToken()
    {
        byte[] bytes = new byte[RANDOM_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return TOKEN_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String prefixOf(String plainToken)
    {
        return plainToken.substring(0, Math.min(20, plainToken.length()));
    }

    private boolean isExpired(ReqActionToken token)
    {
        return token.getExpireTime() != null && token.getExpireTime().before(new Date());
    }

    private boolean isUsed(ReqActionToken token)
    {
        return token.getLastUsedTime() != null;
    }

    private String hashToken(String plainToken)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(plainToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hashed)
            {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new ServiceException("动作Token哈希失败");
        }
    }

    private String firstNotEmpty(String... values)
    {
        for (String value : values)
        {
            if (StringUtils.isNotEmpty(value))
            {
                return value;
            }
        }
        return "";
    }

    private String projectInitInstructionContent(String prompt, String targetMethod, String actionToken, Long projectId, Long variantId)
    {
        return prompt
                + "\n请按全局 skill `reqflow-mcp` 执行 Reqflow 项目接入初始化。"
                + "\nmcpServer: " + PROJECT_INIT_MCP_SERVER
                + "\ntoolName: " + PROJECT_INIT_TOOL_NAME
                + "\nmcpTool: " + PROJECT_INIT_MCP_SERVER + "." + PROJECT_INIT_TOOL_NAME
                + "\ntargetMethod: " + targetMethod
                + "\nprojectId: " + projectId
                + "\nvariantId: " + variantId
                + "\nactionToken: " + actionToken
                + "\n" + ACTION_TOKEN_USAGE_RULE
                + "\n要求：actionToken 是 publish_repository_index 的 arguments.actionToken，不是 X-MCP-Key。";
    }

    private static class GeneratedToken
    {
        private final String plainToken;

        private final String hash;

        private GeneratedToken(String plainToken, String hash)
        {
            this.plainToken = plainToken;
            this.hash = hash;
        }
    }
}
