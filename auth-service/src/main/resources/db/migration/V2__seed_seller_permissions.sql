-- ============================================================
-- V2: SEED ROLE_SELLER PERMISSIONS
-- ============================================================

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_SELLER'
  AND p.name IN (
    'user:read', 'user:write',
    'product:read', 'product:write', 'product:delete',
    'inventory:manage',
    'order:read', 'order:write',
    'payment:read',
    'review:moderate'
  )
ON CONFLICT DO NOTHING;
