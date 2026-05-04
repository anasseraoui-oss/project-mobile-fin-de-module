CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    role VARCHAR(50) NOT NULL, -- SUPER_ADMIN, ORGANISATION, FORMATEUR, APPRENANT
    provider VARCHAR(50),      -- LOCAL, GOOGLE, FACEBOOK
    provider_id VARCHAR(255),
    avatar_key VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE organisations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    logo_key VARCHAR(255),
    banner_key VARCHAR(255),
    owner_id UUID REFERENCES users(id),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE formations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    thumbnail_key VARCHAR(255),
    organisation_id UUID REFERENCES organisations(id),
    level VARCHAR(50), -- BEGINNER, INTERMEDIATE, ADVANCED
    language VARCHAR(50),
    price DECIMAL(10,2) DEFAULT 0.0,
    is_published BOOLEAN DEFAULT FALSE,
    prerequisite_ids UUID[],
    certificate_template_key VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE courses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    formation_id UUID REFERENCES formations(id),
    order_index INT,
    is_published BOOLEAN DEFAULT FALSE
);

CREATE TABLE seances (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    course_id UUID REFERENCES courses(id),
    type VARCHAR(50), -- LIVE, RECORDED
    video_key VARCHAR(255),
    duration_seconds INT,
    meet_link VARCHAR(255),
    scheduled_at TIMESTAMP,
    order_index INT,
    is_published BOOLEAN DEFAULT FALSE
);

CREATE TABLE quizzes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_id UUID UNIQUE REFERENCES courses(id),
    pass_score INT NOT NULL,
    max_attempts INT,
    timer_seconds INT
);

CREATE TABLE quiz_questions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    quiz_id UUID REFERENCES quizzes(id),
    question TEXT NOT NULL,
    type VARCHAR(50), -- MCQ, TRUE_FALSE, OPEN
    options JSONB,
    correct_answer JSONB,
    points INT DEFAULT 1,
    order_index INT
);

CREATE TABLE enrollments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    formation_id UUID REFERENCES formations(id),
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    certificate_key VARCHAR(255),
    UNIQUE(user_id, formation_id)
);

CREATE TABLE progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    seance_id UUID REFERENCES seances(id),
    watched_seconds INT DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    last_watched_at TIMESTAMP,
    UNIQUE(user_id, seance_id)
);

CREATE TABLE quiz_attempts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    quiz_id UUID REFERENCES quizzes(id),
    answers JSONB,
    score INT,
    passed BOOLEAN,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendances (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    seance_id UUID REFERENCES seances(id),
    qr_code_token VARCHAR(255),
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    type VARCHAR(50),
    title VARCHAR(255),
    body TEXT,
    data JSONB,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
