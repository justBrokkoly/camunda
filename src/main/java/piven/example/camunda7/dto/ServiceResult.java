package piven.example.camunda7.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public class ServiceResult {
    private String requestId;
    private String status;
    private String scenario;
    private String type;
    private Boolean success;
    private String message;
    private Object data;
}
