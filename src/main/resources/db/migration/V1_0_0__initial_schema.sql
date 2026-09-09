CREATE TABLE users (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(120)  NOT NULL,
    email         VARCHAR(180)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- type: INCOME | EXPENSE  (as "categorias positivas e negativas")
-- Catálogo padrão. É só a fonte do seed: nenhuma transação referencia esta tabela.
CREATE TABLE category_templates (
    id         SMALLSERIAL PRIMARY KEY,
    name       VARCHAR(80)  NOT NULL,
    type       VARCHAR(10)  NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
    color      VARCHAR(7)   NOT NULL,
    icon       VARCHAR(40),
    sort_order SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uq_template UNIQUE (name, type)
);

-- Categorias reais: sempre de um usuário, sem exceção.
CREATE TABLE categories (
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(80)  NOT NULL,
    type       VARCHAR(10)  NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
    color      VARCHAR(7)   NOT NULL DEFAULT '#6B7280',
    icon       VARCHAR(40),
    is_default BOOLEAN      NOT NULL DEFAULT FALSE,  -- veio do catálogo padrão
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_category_per_user UNIQUE (user_id, name, type),
    -- Alvo das FKs compostas: garante que quem referencia a categoria
    -- referencia junto o dono dela.
    CONSTRAINT uq_categories_id_user UNIQUE (id, user_id)
);

CREATE INDEX idx_categories_user ON categories (user_id, type);

CREATE TABLE goals (
    id            BIGSERIAL PRIMARY KEY,
    user_id       UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name          VARCHAR(120)  NOT NULL,
    description   TEXT,
    target_amount NUMERIC(14,2) NOT NULL CHECK (target_amount > 0),
    target_date   DATE          NOT NULL,
    status        VARCHAR(12)   NOT NULL DEFAULT 'ACTIVE'
                  CHECK (status IN ('ACTIVE','COMPLETED','CANCELLED')),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_goals_id_user UNIQUE (id, user_id)
);

CREATE INDEX idx_goals_user ON goals (user_id, status);

-- frequency: MONTHLY | WEEKLY | YEARLY
CREATE TABLE recurring_transactions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id     BIGINT        NOT NULL,
    type            VARCHAR(10)   NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
    description     VARCHAR(180)  NOT NULL,
    amount          NUMERIC(14,2) NOT NULL CHECK (amount > 0),
    frequency       VARCHAR(10)   NOT NULL CHECK (frequency IN ('MONTHLY','WEEKLY','YEARLY')),
    day_of_month    SMALLINT      CHECK (day_of_month BETWEEN 1 AND 31),
    start_date      DATE          NOT NULL,
    end_date        DATE,
    next_occurrence DATE          NOT NULL,
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT uq_recurring_id_user UNIQUE (id, user_id),
    CONSTRAINT fk_recurring_category
        FOREIGN KEY (category_id, user_id) REFERENCES categories (id, user_id)
);

CREATE INDEX idx_recurring_due      ON recurring_transactions (user_id, active, next_occurrence);
CREATE INDEX idx_recurring_category ON recurring_transactions (category_id, user_id);

CREATE TABLE transactions (
    id                       BIGSERIAL PRIMARY KEY,
    user_id                  UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id              BIGINT        NOT NULL,
    recurring_transaction_id BIGINT,
    goal_id                  BIGINT,
    type                     VARCHAR(10)   NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
    description              VARCHAR(180)  NOT NULL,
    amount                   NUMERIC(14,2) NOT NULL CHECK (amount > 0),
    transaction_date         DATE          NOT NULL,
    created_at               TIMESTAMPTZ   NOT NULL DEFAULT now(),

    -- FKs compostas: a categoria/recorrência/meta tem de ser do MESMO usuário.
    CONSTRAINT fk_tx_category
        FOREIGN KEY (category_id, user_id) REFERENCES categories (id, user_id),
    CONSTRAINT fk_tx_recurring
        FOREIGN KEY (recurring_transaction_id, user_id)
        REFERENCES recurring_transactions (id, user_id)
        ON DELETE SET NULL (recurring_transaction_id),
    CONSTRAINT fk_tx_goal
        FOREIGN KEY (goal_id, user_id) REFERENCES goals (id, user_id)
        ON DELETE SET NULL (goal_id)
);

CREATE INDEX idx_tx_user_date ON transactions (user_id, transaction_date DESC);
CREATE INDEX idx_tx_user_cat  ON transactions (user_id, category_id);
CREATE INDEX idx_tx_recurring ON transactions (recurring_transaction_id, user_id);
CREATE INDEX idx_tx_goal      ON transactions (goal_id, user_id);

CREATE TABLE goal_contributions (
    id             BIGSERIAL PRIMARY KEY,
    goal_id        BIGINT        NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    transaction_id BIGINT        REFERENCES transactions(id) ON DELETE SET NULL,
    amount         NUMERIC(14,2) NOT NULL CHECK (amount <> 0),
    contributed_at DATE          NOT NULL,
    note           VARCHAR(180),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_contrib_goal ON goal_contributions (goal_id, contributed_at);
CREATE INDEX idx_contrib_tx   ON goal_contributions (transaction_id);

-- type: FIXED_INCOME | VARIABLE_INCOME | CRYPTO | FUND | OTHER
CREATE TABLE investments (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(120)  NOT NULL,
    type            VARCHAR(20)   NOT NULL,
    invested_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    current_amount  NUMERIC(14,2) NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_investments_user ON investments (user_id);
