-- ==========================================================
-- V2: Sync database schema with evolved JPA entities
-- Adds missing tables and columns to match entity definitions
-- ==========================================================

-- ========== ALTER existing tables ==========

-- users: add columns required by User.java entity
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS organisation_id UUID;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS fcm_token VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;

-- organisations: add columns required by Organisation.java entity
ALTER TABLE organisations ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE organisations ADD COLUMN IF NOT EXISTS sector VARCHAR(255);
ALTER TABLE organisations ADD COLUMN IF NOT EXISTS website VARCHAR(255);
ALTER TABLE organisations ADD COLUMN IF NOT EXISTS is_default BOOLEAN DEFAULT FALSE;
ALTER TABLE organisations ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'PENDING';
ALTER TABLE organisations ADD COLUMN IF NOT EXISTS validated_at TIMESTAMP;
ALTER TABLE organisations ADD COLUMN IF NOT EXISTS validated_by UUID;

-- formations: add columns required by Formation.java entity
ALTER TABLE formations ADD COLUMN IF NOT EXISTS slug VARCHAR(255);
ALTER TABLE formations ADD COLUMN IF NOT EXISTS cover_image_key VARCHAR(255);
ALTER TABLE formations ADD COLUMN IF NOT EXISTS currency VARCHAR(50) DEFAULT 'MAD';
ALTER TABLE formations ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'BROUILLON';
ALTER TABLE formations ADD COLUMN IF NOT EXISTS formateur_id UUID;
ALTER TABLE formations ADD COLUMN IF NOT EXISTS total_duration INTEGER;
ALTER TABLE formations ADD COLUMN IF NOT EXISTS max_students INTEGER;
ALTER TABLE formations ADD COLUMN IF NOT EXISTS prerequisites_text TEXT;
ALTER TABLE formations ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE formations ADD COLUMN IF NOT EXISTS published_at TIMESTAMP;

-- courses: add columns required by Course.java entity
ALTER TABLE courses ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'A_VENIR';
ALTER TABLE courses ADD COLUMN IF NOT EXISTS presence_threshold INTEGER DEFAULT 80;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS quiz_pass_score INTEGER DEFAULT 70;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS estimated_duration INTEGER;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- seances: add columns required by Seance.java entity
ALTER TABLE seances ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE seances ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'PLANIFIEE';
ALTER TABLE seances ADD COLUMN IF NOT EXISTS formateur_id UUID;
ALTER TABLE seances ADD COLUMN IF NOT EXISTS qr_code_token VARCHAR(255);
ALTER TABLE seances ADD COLUMN IF NOT EXISTS qr_code_expires_at TIMESTAMP;
ALTER TABLE seances ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- quizzes: add columns required by Quiz.java entity
ALTER TABLE quizzes ADD COLUMN IF NOT EXISTS title VARCHAR(255);
ALTER TABLE quizzes ADD COLUMN IF NOT EXISTS is_published BOOLEAN DEFAULT FALSE;

-- ========== CREATE missing tables ==========

-- Inscriptions (Inscription.java)
CREATE TABLE IF NOT EXISTS inscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    apprenant_id UUID NOT NULL,
    formation_id UUID NOT NULL REFERENCES formations(id),
    status VARCHAR(50) NOT NULL DEFAULT 'EN_COURS',
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    payment_id VARCHAR(255),
    access_expires_at TIMESTAMP,
    UNIQUE(apprenant_id, formation_id)
);

-- Presences (Presence.java)
CREATE TABLE IF NOT EXISTS presences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    apprenant_id UUID NOT NULL,
    seance_id UUID NOT NULL REFERENCES seances(id),
    status VARCHAR(50) NOT NULL,
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    validation_method VARCHAR(50) NOT NULL,
    ip_address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(apprenant_id, seance_id)
);

-- Progressions (Progression.java)
CREATE TABLE IF NOT EXISTS progressions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    apprenant_id UUID NOT NULL,
    cours_id UUID NOT NULL,
    formation_id UUID NOT NULL,
    presence_rate DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    quiz_status VARCHAR(50) NOT NULL DEFAULT 'NON_COMMENCE',
    completion_date TIMESTAMP,
    is_unlocked BOOLEAN NOT NULL DEFAULT FALSE,
    unlocked_at TIMESTAMP,
    UNIQUE(apprenant_id, cours_id)
);

-- Certificats (Certificat.java)
CREATE TABLE IF NOT EXISTS certificats (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    apprenant_id UUID NOT NULL,
    formation_id UUID NOT NULL,
    organisation_id UUID,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    pdf_key VARCHAR(255),
    verification_code UUID NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    average_score DECIMAL(5,2),
    expires_at TIMESTAMP,
    UNIQUE(apprenant_id, formation_id)
);

-- TentativeQuiz (TentativeQuiz.java)
CREATE TABLE IF NOT EXISTS tentatives_quiz (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    apprenant_id UUID NOT NULL,
    quiz_id UUID NOT NULL REFERENCES quizzes(id),
    started_at TIMESTAMP,
    submitted_at TIMESTAMP,
    score DECIMAL(5,2),
    status VARCHAR(50) NOT NULL DEFAULT 'EN_COURS',
    answers_snapshot JSONB,
    attempt_number INTEGER NOT NULL
);

-- QuizReponses (QuizReponse.java)
CREATE TABLE IF NOT EXISTS quiz_reponses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question_id UUID NOT NULL REFERENCES quiz_questions(id),
    text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE
);

-- ForumPosts (ForumPost.java)
CREATE TABLE IF NOT EXISTS forum_posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    author_id UUID REFERENCES users(id),
    seance_id UUID REFERENCES seances(id),
    content TEXT,
    parent_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Self-referencing FK added separately to avoid ordering issues
ALTER TABLE forum_posts DROP CONSTRAINT IF EXISTS fk_forum_posts_parent;
ALTER TABLE forum_posts ADD CONSTRAINT fk_forum_posts_parent
    FOREIGN KEY (parent_id) REFERENCES forum_posts(id);

-- Messages (Message.java)
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sender_id UUID REFERENCES users(id),
    receiver_id UUID REFERENCES users(id),
    content TEXT,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE
);
