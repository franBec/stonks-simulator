CREATE TABLE IF NOT EXISTS chaosevent_incident_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    headline VARCHAR(512) NOT NULL,
    target_symbol VARCHAR(4) NOT NULL,
    impact_percent DECIMAL(10,2) NOT NULL,
    explanation VARCHAR(2048),
    affected_symbols VARCHAR(2048),
    source_headline VARCHAR(512),
    event_type VARCHAR(32),
    event_severity VARCHAR(16),
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
