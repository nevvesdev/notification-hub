-- Garante índice para consultas por status e melhora rastreabilidade de falhas
CREATE INDEX IF NOT EXISTS idx_notifications_error ON notifications (status)
    WHERE status IN ('FAILED', 'DEAD_LETTERED');