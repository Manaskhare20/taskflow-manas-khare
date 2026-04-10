-- Seed user: test@example.com / password123 (bcrypt cost 12)
INSERT INTO users (id, name, email, password, created_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440001',
    'Test User',
    'test@example.com',
    '$2b$12$gjo0wFadtDZlaX9xdbVUn.0K7iAgtE0TrBzK/HaHac2gb/H7Llj9G',
    NOW()
);

INSERT INTO projects (id, name, description, owner_id, created_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440002',
    'Demo Project',
    'Seeded project for TaskFlow',
    '550e8400-e29b-41d4-a716-446655440001',
    NOW()
);

INSERT INTO tasks (id, title, description, status, priority, project_id, assignee_id, creator_id, due_date, created_at, updated_at)
VALUES
    (
        '550e8400-e29b-41d4-a716-446655440011',
        'Draft project plan',
        'Outline milestones',
        'todo',
        'high',
        '550e8400-e29b-41d4-a716-446655440002',
        '550e8400-e29b-41d4-a716-446655440001',
        '550e8400-e29b-41d4-a716-446655440001',
        CURRENT_DATE + 7,
        NOW(),
        NOW()
    ),
    (
        '550e8400-e29b-41d4-a716-446655440012',
        'Implement API',
        'Spring Boot endpoints',
        'in_progress',
        'medium',
        '550e8400-e29b-41d4-a716-446655440002',
        NULL,
        '550e8400-e29b-41d4-a716-446655440001',
        NULL,
        NOW(),
        NOW()
    ),
    (
        '550e8400-e29b-41d4-a716-446655440013',
        'Write documentation',
        NULL,
        'done',
        'low',
        '550e8400-e29b-41d4-a716-446655440002',
        NULL,
        '550e8400-e29b-41d4-a716-446655440001',
        CURRENT_DATE - 1,
        NOW(),
        NOW()
    );
