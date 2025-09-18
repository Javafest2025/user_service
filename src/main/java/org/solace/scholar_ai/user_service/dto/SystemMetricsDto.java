package org.solace.scholar_ai.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetricsDto {
    private PerformanceMetrics performance;
    private ResourceUsage resources;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private long totalRequests;
        private double requestChange;
        private double avgResponseTime;
        private double responseTimeChange;
        private double errorRate;
        private double errorRateChange;
        private double networkIn; // GB/s
        private double networkInChange;
        private double networkOut; // MB/s
        private double networkOutChange;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceUsage {
        private double cpuUsage; // percentage
        private double memoryUsage; // percentage
        private double diskUsage; // percentage
    }
}