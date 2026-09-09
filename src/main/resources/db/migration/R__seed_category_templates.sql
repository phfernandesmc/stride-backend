-- Migração repetível: roda de novo sempre que o checksum deste arquivo mudar,
-- e sempre DEPOIS das versionadas. Por isso precisa ser idempotente.
INSERT INTO category_templates (name, type, color, icon, sort_order) VALUES
    -- Entradas
    ('Salário',            'INCOME',  '#16A34A', 'wallet',          10),
    ('Renda extra',        'INCOME',  '#22C55E', 'briefcase',       20),
    ('Rendimentos',        'INCOME',  '#10B981', 'trending-up',     30),
    ('Vendas',             'INCOME',  '#14B8A6', 'tag',             40),
    ('Reembolso',          'INCOME',  '#06B6D4', 'undo-2',          50),
    ('Presente',           'INCOME',  '#84CC16', 'gift',            60),
    ('Outras entradas',    'INCOME',  '#6B7280', 'circle-plus',    999),

    -- Saídas
    ('Moradia',            'EXPENSE', '#DC2626', 'house',           10),
    ('Contas de consumo',  'EXPENSE', '#EA580C', 'plug-zap',        20),
    ('Supermercado',       'EXPENSE', '#F59E0B', 'shopping-cart',   30),
    ('Alimentação',        'EXPENSE', '#F97316', 'utensils',        40),
    ('Transporte',         'EXPENSE', '#0EA5E9', 'car',             50),
    ('Saúde',              'EXPENSE', '#EF4444', 'heart-pulse',     60),
    ('Educação',           'EXPENSE', '#6366F1', 'graduation-cap',  70),
    ('Lazer',              'EXPENSE', '#A855F7', 'party-popper',    80),
    ('Assinaturas',        'EXPENSE', '#8B5CF6', 'repeat',          90),
    ('Vestuário',          'EXPENSE', '#EC4899', 'shirt',          100),
    ('Viagem',             'EXPENSE', '#3B82F6', 'plane',          110),
    ('Pets',               'EXPENSE', '#D97706', 'paw-print',      120),
    ('Impostos e taxas',   'EXPENSE', '#78716C', 'landmark',       130),
    ('Dívidas',            'EXPENSE', '#B91C1C', 'credit-card',    140),
    ('Outras saídas',      'EXPENSE', '#6B7280', 'circle-minus',   999)
ON CONFLICT (name, type) DO UPDATE
    SET color      = EXCLUDED.color,
        icon       = EXCLUDED.icon,
        sort_order = EXCLUDED.sort_order;
