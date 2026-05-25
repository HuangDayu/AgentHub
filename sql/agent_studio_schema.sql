-- Agent Studio消息处理相关表

-- Run注册表
CREATE TABLE IF NOT EXISTS run_registrations (
    id varchar(64) NOT NULL PRIMARY KEY,
    project varchar(255) NOT NULL,
    name varchar(255) NOT NULL,
    timestamp timestamp NOT NULL,
    pid integer NOT NULL,
    status varchar(50) NOT NULL,
    run_dir TEXT,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_run_registrations_project ON run_registrations(project);
CREATE INDEX idx_run_registrations_status ON run_registrations(status);
CREATE INDEX idx_run_registrations_timestamp ON run_registrations(timestamp);

-- 消息推送表
CREATE TABLE IF NOT EXISTS message_pushes (
    id varchar(64) NOT NULL PRIMARY KEY,
    message_id varchar(64) NOT NULL,
    run_id varchar(64) NOT NULL,
    role varchar(50) NOT NULL,
    content TEXT,
    metadata TEXT,
    timestamp timestamp NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_message_pushes_run_id ON message_pushes(run_id);
CREATE INDEX idx_message_pushes_message_id ON message_pushes(message_id);
CREATE INDEX idx_message_pushes_timestamp ON message_pushes(timestamp);

-- 用户输入请求表
CREATE TABLE IF NOT EXISTS user_input_requests (
    id varchar(64) NOT NULL PRIMARY KEY,
    request_id varchar(64) NOT NULL,
    run_id varchar(64) NOT NULL,
    agent_id varchar(64) NOT NULL,
    agent_name varchar(255) NOT NULL,
    structured_input TEXT,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_input_requests_run_id ON user_input_requests(run_id);
CREATE INDEX idx_user_input_requests_request_id ON user_input_requests(request_id);
CREATE INDEX idx_user_input_requests_agent_id ON user_input_requests(agent_id);
