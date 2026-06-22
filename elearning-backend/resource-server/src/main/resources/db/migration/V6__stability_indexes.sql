CREATE INDEX IF NOT EXISTS idx_seances_live_due
    ON seances(type, status, scheduled_at)
    WHERE type = 'LIVE';

CREATE INDEX IF NOT EXISTS idx_notifications_user_created
    ON notifications(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_notifications_unread
    ON notifications(user_id, is_read)
    WHERE is_read = false;

CREATE INDEX IF NOT EXISTS idx_inscriptions_formation_status
    ON inscriptions(formation_id, status);

CREATE INDEX IF NOT EXISTS idx_tentatives_quiz_lookup
    ON tentatives_quiz(apprenant_id, quiz_id, status, attempt_number DESC);

CREATE INDEX IF NOT EXISTS idx_progress_user_seance
    ON progress(user_id, seance_id);
