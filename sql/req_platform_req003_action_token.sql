-- REQ-003 项目页签化与统一需求流转平台UI：MCP动作Token

create table if not exists req_action_token (
  token_id bigint(20) not null auto_increment comment 'Token ID',
  action_type varchar(64) not null comment '动作类型',
  token_prefix varchar(64) not null comment 'Token前缀',
  token_hash char(64) not null comment 'Token哈希',
  target_method varchar(100) not null comment '目标MCP方法',
  project_id bigint(20) default null comment '项目ID',
  variant_id bigint(20) default null comment '分支ID',
  demand_id bigint(20) default null comment '需求ID',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  expire_time datetime default null comment '过期时间',
  last_used_time datetime default null comment '最近使用时间',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default null comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default null comment '更新时间',
  remark varchar(500) default null comment '备注',
  primary key (token_id),
  unique key uk_req_action_token_hash (token_hash),
  key idx_req_action_token_context (action_type, project_id, variant_id, demand_id, status)
) engine=innodb auto_increment=1 comment='MCP动作Token表';
