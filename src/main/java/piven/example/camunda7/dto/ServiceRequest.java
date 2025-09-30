package piven.example.camunda7.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;
@Setter
@Getter
@RequiredArgsConstructor
public class ServiceRequest {
    private String requestId;
    private Object response;
    private String status;
    private String type;
    private String scenario;
    private Map<String, Object> mapResponse;
    private Boolean enabled;
}
