-- V008: Create monthly_reports table for tracking report generation
-- Feature: Food Analytics Dashboard and Monthly Reporting
-- Date: 2026-02-06

CREATE TABLE monthly_reports (
    id BIGSERIAL PRIMARY KEY,
    report_date DATE NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    generated_at TIMESTAMP,
    total_items INTEGER,
    total_views BIGINT,
    total_orders BIGINT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'GENERATING', 'SENT', 'FAILED'))
);

CREATE INDEX idx_monthly_report_date ON monthly_reports(report_date);

COMMENT ON TABLE monthly_reports IS 'Tracks monthly analytics report generation and delivery status';
COMMENT ON COLUMN monthly_reports.report_date IS 'First day of the month being reported (e.g., 2026-02-01 for February 2026 report)';
COMMENT ON COLUMN monthly_reports.status IS 'Current report status: PENDING, GENERATING, SENT, or FAILED';
COMMENT ON COLUMN monthly_reports.generated_at IS 'Timestamp when report was successfully generated and sent';
COMMENT ON COLUMN monthly_reports.total_items IS 'Count of food items included in report (cached for quick reference)';
COMMENT ON COLUMN monthly_reports.total_views IS 'Sum of all view counts in report (cached)';
COMMENT ON COLUMN monthly_reports.total_orders IS 'Sum of all order counts in report (cached)';
COMMENT ON COLUMN monthly_reports.error_message IS 'Error details if report generation/sending failed';
