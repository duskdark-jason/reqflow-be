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

    private static final String ACTION_TOKEN_USAGE_RULE = "有效期：当前动作内有效，最长保留24小时，过期或已使用后需重新生成。";

    private static final String REQUIREMENT_ANALYSIS_STAGE_TOKEN_USAGE_RULE =
            "有效期：当前需求可行性分析阶段内有效，流转到下一阶段后即失效；最长保留24小时，可在本阶段多次用于本地迭代后的评估报告回写。";

    private static final String REQUIREMENT_GENERATE_STAGE_TOKEN_USAGE_RULE =
            "有效期：当前需求设计阶段内有效，流转到下一阶段后即失效；最长保留24小时，可在本阶段多次用于本地迭代后的需求设计回写。";

    private static final String DEVELOPMENT_STAGE_TOKEN_USAGE_RULE =
            "有效期：当前开发阶段内有效，流转到待验收后即失效；最长保留24小时，可在本阶段多次用于执行计划、执行报告和 Review 报告回写。";

    private static final String REPAIR_STAGE_TOKEN_USAGE_RULE =
            "有效期：当前返修阶段内有效，流转到待验收后即失效；最长保留24小时，可在本阶段多次用于执行报告和 Review 报告回写。";

    private static final String CLOSEOUT_STAGE_TOKEN_USAGE_RULE =
            "有效期：当前合并归档阶段内有效，流转到办结后即失效；最长保留24小时，可在本阶段多次用于各仓库知识库索引发布。";

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
        instruction.setContent(projectInitInstructionContent(instruction.getToken()));
        return instruction;
    }

    @Override
    @Transactional
    public ReqActionInstruction createInstruction(String actionType, Long projectId, Long variantId, Long demandId,
            String targetMethod, String prompt, String copyLabel, String operator)
    {
        return createInstruction(actionType, projectId, variantId, demandId, targetMethod, prompt, copyLabel,
                operator, null);
    }

    @Override
    @Transactional
    public ReqActionInstruction createInstruction(String actionType, Long projectId, Long variantId, Long demandId,
            String targetMethod, String prompt, String copyLabel, String operator, String remark)
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
        token.setRemark(remark);
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
                + "\n" + usageRule(actionType, targetMethod));
        return instruction;
    }

    @Override
    @Transactional
    public ReqActionToken resolveToken(String plainToken)
    {
        ReqActionToken token = selectUsableToken(plainToken);
        boolean reusableStageToken = isReusableStageToken(token);
        if (isUsed(token) && !reusableStageToken)
        {
            throw new ServiceException("动作Token已使用，请重新生成");
        }
        // 解析成功后记录使用时间。普通动作通过条件更新保证一次性消费，开发阶段 Token 仅刷新最近使用时间。
        if (token.getTokenId() != null)
        {
            int updated = reusableStageToken
                    ? actionTokenMapper.touchLastUsed(token.getTokenId())
                    : actionTokenMapper.updateLastUsed(token.getTokenId());
            if (updated <= 0)
            {
                throw new ServiceException("动作Token已使用，请重新生成");
            }
        }
        return token;
    }

    @Override
    public ReqActionToken resolveTokenForContext(String plainToken)
    {
        ReqActionToken token = selectUsableToken(plainToken);
        if (isUsed(token) && !isReusableStageToken(token))
        {
            throw new ServiceException("动作Token已使用，请重新生成");
        }
        return token;
    }

    private ReqActionToken selectUsableToken(String plainToken)
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

    private boolean isReusableStageToken(ReqActionToken token)
    {
        if (ACTION_REQUIREMENT_PLAN.equals(token.getActionType()))
        {
            return TARGET_REQUIREMENT_ANALYSIS.equals(token.getTargetMethod())
                    || TARGET_REQUIREMENT_GENERATE.equals(token.getTargetMethod());
        }
        if (ACTION_REQUIREMENT_DEVELOP.equals(token.getActionType()))
        {
            return TARGET_REQUIREMENT_DEVELOP.equals(token.getTargetMethod())
                    || TARGET_REQUIREMENT_REPAIR.equals(token.getTargetMethod());
        }
        return ACTION_REQUIREMENT_CLOSEOUT.equals(token.getActionType())
                && TARGET_PUBLISH_REPOSITORY_INDEX.equals(token.getTargetMethod());
    }

    private String usageRule(String actionType, String targetMethod)
    {
        if (ACTION_REQUIREMENT_PLAN.equals(actionType) && TARGET_REQUIREMENT_ANALYSIS.equals(targetMethod))
        {
            return REQUIREMENT_ANALYSIS_STAGE_TOKEN_USAGE_RULE;
        }
        if (ACTION_REQUIREMENT_PLAN.equals(actionType) && TARGET_REQUIREMENT_GENERATE.equals(targetMethod))
        {
            return REQUIREMENT_GENERATE_STAGE_TOKEN_USAGE_RULE;
        }
        if (ACTION_REQUIREMENT_DEVELOP.equals(actionType) && TARGET_REQUIREMENT_DEVELOP.equals(targetMethod))
        {
            return DEVELOPMENT_STAGE_TOKEN_USAGE_RULE;
        }
        if (ACTION_REQUIREMENT_DEVELOP.equals(actionType) && TARGET_REQUIREMENT_REPAIR.equals(targetMethod))
        {
            return REPAIR_STAGE_TOKEN_USAGE_RULE;
        }
        if (ACTION_REQUIREMENT_CLOSEOUT.equals(actionType) && TARGET_PUBLISH_REPOSITORY_INDEX.equals(targetMethod))
        {
            return CLOSEOUT_STAGE_TOKEN_USAGE_RULE;
        }
        return ACTION_TOKEN_USAGE_RULE;
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

    private String projectInitInstructionContent(String actionToken)
    {
        return "请按全局 skill `reqflow-mcp` 执行 Reqflow 项目接入初始化。"
                + "\nmcpServer: " + PROJECT_INIT_MCP_SERVER
                + "\ntoolName: " + PROJECT_INIT_TOOL_NAME
                + "\nmcpTool: " + PROJECT_INIT_MCP_SERVER + "." + PROJECT_INIT_TOOL_NAME
                + "\nactionToken: " + actionToken;
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
