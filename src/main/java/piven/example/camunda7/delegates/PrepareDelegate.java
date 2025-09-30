package piven.example.camunda7.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import piven.example.camunda7.dto.ServiceRequest;
import piven.example.camunda7.dto.ServiceResult;
import piven.example.camunda7.tasks.PrepareService;

@Component
public class PrepareDelegate implements JavaDelegate {
    private final PrepareService prepareService;
    public PrepareDelegate(PrepareService prepareService) {
        this.prepareService = prepareService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var request = createServiceRequest(execution);
        var result = prepareService.prepareResponse(request);
        execution.setVariable(getResponseVariableName(execution), result);
    }

    private ServiceRequest createServiceRequest(DelegateExecution execution) {
        var request = new ServiceRequest();
        request.setRequestId((String) execution.getVariable("requestId"));
        request.setResponse(execution.getVariable("response"));
        request.setStatus((String) execution.getVariable("status"));
        request.setType((String) execution.getVariable("type"));
        request.setEnabled((Boolean) execution.getVariable("enabled"));
        return request;
    }

    private String getResponseVariableName(DelegateExecution execution) {
        return (String) execution.getVariable("responseAlias");
    }
}
