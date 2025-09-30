package piven.example.camunda7.delegates;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piven.example.camunda7.dto.ServiceRequest;
import piven.example.camunda7.dto.ServiceResult;
import piven.example.camunda7.tasks.PublicationService;

import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNullElse;

@Component("publicationSimpleDelegate")
@RequiredArgsConstructor
public class PublicationSimpleDelegate implements JavaDelegate {
    private final PublicationService publicationService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var request = createServiceRequest(execution);
        var result = publicationService.publish(request);

        String responseAlias = (String) execution.getVariable("responseAlias");
        execution.setVariable(requireNonNullElse(responseAlias, "response"), result);
    }

    @SuppressWarnings("unchecked")
    private ServiceRequest createServiceRequest(DelegateExecution execution) {
        var request = new ServiceRequest();
        request.setRequestId((String) execution.getVariable("requestId"));
        request.setResponse(execution.getVariable("response"));
        request.setStatus((String) execution.getVariable("status"));
        request.setType((String) execution.getVariable("type"));
        request.setEnabled((Boolean) execution.getVariable("enabled"));
        request.setMapResponse((Map<String, Object>) execution.getVariable("mapResponse"));
        return request;
    }
}
