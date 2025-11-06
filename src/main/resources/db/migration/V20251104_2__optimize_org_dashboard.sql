-- Optimize organization dashboard queries with supporting indexes
-- Requests status/counts and latest pending by materials

-- Index to accelerate counts by status and ordering by created_at
CREATE INDEX IF NOT EXISTS idx_requests_status_created_at
    ON requests (status, created_at DESC);

-- Index to accelerate counts by organization and status
CREATE INDEX IF NOT EXISTS idx_requests_org_status
    ON requests (organization_id, status);

-- Indexes for request_materials lookups used by EXISTS/LEFT JOIN
CREATE INDEX IF NOT EXISTS idx_request_materials_request
    ON request_materials (request_id);

CREATE INDEX IF NOT EXISTS idx_request_materials_material
    ON request_materials (material_id);

-- Composite indexes to help JOIN/EXISTS filters either way
CREATE INDEX IF NOT EXISTS idx_request_materials_req_mat
    ON request_materials (request_id, material_id);

CREATE INDEX IF NOT EXISTS idx_request_materials_mat_req
    ON request_materials (material_id, request_id);
