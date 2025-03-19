CREATE TABLE IF NOT EXISTS chat_memories (
    memory_id BIGSERIAL PRIMARY KEY,
    memory_key VARCHAR(255) NOT NULL UNIQUE,
    messages TEXT NOT NULL
);