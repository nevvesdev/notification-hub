-- Baseline schema: notifications audit log
CREATE TABLE IF NOT EXISTS notifications (
    id          UUID        PRIMARY KEY,
    recipient   VARCHAR(255) NOT NULL,
    channel     VARCHAR(50)  NOT NULL,
    subject     VARCHAR(500),
    template    VARCHAR(100) NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    attempts    INT          NOT NULL DEFAULT 0,
    error       TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_notifications_status    ON notifications (status);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications (recipient);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications (created_at DESC);