package com.foodshop.domain;

/**
 * Status of a monthly analytics report.
 * Tracks the lifecycle of report generation and delivery.
 */
public enum ReportStatus {
    /**
     * Report generation scheduled but not started.
     */
    PENDING,
    
    /**
     * Report generation in progress.
     */
    GENERATING,
    
    /**
     * Report successfully generated and emailed to all admins.
     */
    SENT,
    
    /**
     * Report generation or sending failed.
     * Check error_message column for details.
     */
    FAILED
}
