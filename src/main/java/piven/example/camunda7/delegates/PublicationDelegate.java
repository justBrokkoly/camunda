package piven.example.camunda7.delegates;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import piven.example.camunda7.dto.ServiceRequest;
import piven.example.camunda7.dto.ServiceResult;
import piven.example.camunda7.tasks.PublicationService;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PublicationDelegate implements JavaDelegate {

    private final PublicationService publicationService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var request = createServiceRequest(execution);
        var result = publicationService.publish(request);
        execution.setVariable(getResponseVariableName(execution), result);
    }

    @SuppressWarnings("unchecked")
    private ServiceRequest createServiceRequest(DelegateExecution execution) {
        var request = new ServiceRequest();
        request.setRequestId((String) execution.getVariable("requestId"));
        request.setResponse(execution.getVariable("response"));
        request.setStatus((String) execution.getVariable("status"));
        request.setType((String) execution.getVariable("type"));
        request.setMapResponse((Map<String, Object>) execution.getVariable("mapResponse"));
        request.setEnabled((Boolean) execution.getVariable("enabled"));
        return request;
    }

    private String getResponseVariableName(DelegateExecution execution) {
        return (String) execution.getVariable("responseAlias");
    }
}
