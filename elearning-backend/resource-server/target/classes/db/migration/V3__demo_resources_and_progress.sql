CREATE TABLE IF NOT EXISTS pedagogical_resources (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    formation_id UUID NOT NULL REFERENCES formations(id),
    course_id UUID REFERENCES courses(id),
    seance_id UUID REFERENCES seances(id),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    object_key VARCHAR(500) NOT NULL,
    bucket_name VARCHAR(100) NOT NULL DEFAULT 'elearning-media',
    mime_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT,
    checksum_sha256 VARCHAR(64),
    is_downloadable BOOLEAN NOT NULL DEFAULT TRUE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pedagogical_resources_formation ON pedagogical_resources(formation_id);
CREATE INDEX IF NOT EXISTS idx_pedagogical_resources_course ON pedagogical_resources(course_id);
CREATE INDEX IF NOT EXISTS idx_pedagogical_resources_seance ON pedagogical_resources(seance_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_pedagogical_resources_object_key ON pedagogical_resources(object_key);

ALTER TABLE seances ADD COLUMN IF NOT EXISTS pdf_key VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_progress_user_seance ON progress(user_id, seance_id);
CREATE INDEX IF NOT EXISTS idx_progress_last_watched_at ON progress(last_watched_at);
