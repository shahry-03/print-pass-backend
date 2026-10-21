CREATE TABLE print_jobs (
    id UUID PRIMARY KEY,
    secret_key VARCHAR(6) NOT NULL UNIQUE,
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    job_status VARCHAR(50) NOT NULL,
    shop_id UUID,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX idx_secret_key ON print_jobs(secret_key);
CREATE INDEX idx_shop_id ON print_jobs(shop_id);
CREATE INDEX idx_job_status ON print_jobs(job_status);
CREATE INDEX idx_expires_at ON print_jobs(expires_at);