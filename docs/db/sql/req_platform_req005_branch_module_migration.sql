ALTER TABLE req_module ADD COLUMN variant_id BIGINT DEFAULT NULL COMMENT '项目分支ID' AFTER project_id;

ALTER TABLE req_module DROP INDEX uk_req_module_code;
CREATE UNIQUE INDEX uk_req_module_code ON req_module (project_id, variant_id, module_code);
CREATE INDEX idx_req_module_project_variant ON req_module (project_id, variant_id);

CREATE INDEX idx_req_memory_project_variant ON req_memory_index (project_id, variant_id);
CREATE INDEX idx_req_index_module_variant ON req_index_module (project_id, variant_id, module_code);
