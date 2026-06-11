-- Local/test-only settings for ReqFlow development.
-- RuoYi reads captchaEnabled from sys_config and caches it in Redis.
-- After running this script against a running service, call:
-- DELETE /system/config/refreshCache
UPDATE sys_config
SET config_value = 'false',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE config_key = 'sys.account.captchaEnabled';
