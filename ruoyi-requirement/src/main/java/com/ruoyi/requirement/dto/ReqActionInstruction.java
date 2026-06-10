package com.ruoyi.requirement.dto;

import java.util.Date;

public class ReqActionInstruction
{
    private String actionType;
    private String targetMethod;
    private String token;
    private String tokenPrefix;
    private String prompt;
    private String content;
    private String copyLabel;
    private Date expireTime;

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getTargetMethod() { return targetMethod; }
    public void setTargetMethod(String targetMethod) { this.targetMethod = targetMethod; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTokenPrefix() { return tokenPrefix; }
    public void setTokenPrefix(String tokenPrefix) { this.tokenPrefix = tokenPrefix; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCopyLabel() { return copyLabel; }
    public void setCopyLabel(String copyLabel) { this.copyLabel = copyLabel; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
}
