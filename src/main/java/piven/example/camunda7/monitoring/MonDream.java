package piven.example.camunda7.monitoring;

import lombok.Builder;
import lombok.Data;

/**
 * DTO для события мониторинга
 */
@Data
@Builder
public class MonDream {
    private String serviceName;
    private String requestId;
    private String bsnKey;
    private String value;
}
