CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE events (
    id              BIGSERIAL PRIMARY KEY,
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    VARCHAR(100) NOT NULL,
    version         BIGINT NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    event_data      JSONB NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (aggregate_type, aggregate_id, version)
);

CREATE TABLE chat_sessions (
    id          BIGSERIAL PRIMARY KEY,
    chat_id     VARCHAR(100) NOT NULL UNIQUE,
    summary     JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE chat_participants (
    id              BIGSERIAL PRIMARY KEY,
    chat_id         VARCHAR(100) NOT NULL REFERENCES chat_sessions(chat_id),
    participant_id  VARCHAR(100) NOT NULL,
    UNIQUE (chat_id, participant_id)
);

CREATE TABLE message_history (
    id          BIGSERIAL PRIMARY KEY,
    message_id  VARCHAR(100) NOT NULL UNIQUE,
    chat_id     VARCHAR(100) NOT NULL,
    role        VARCHAR(50) NOT NULL,
    owned_by    VARCHAR(100) NOT NULL,
    content     JSONB NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE embeddings (
    id          BIGSERIAL PRIMARY KEY,
    model       VARCHAR(100) NOT NULL,
    actor       VARCHAR(100) NOT NULL,
    dimension   INTEGER NOT NULL,
    embedding   vector(1536),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE idempotency_keys (
    id          VARCHAR(100) PRIMARY KEY,
    resource_id VARCHAR(100) NOT NULL UNIQUE,
    state       VARCHAR(50) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
