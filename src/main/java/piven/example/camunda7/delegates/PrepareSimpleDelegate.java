package piven.example.camunda7.delegates;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import piven.example.camunda7.dto.ServiceRequest;
import piven.example.camunda7.tasks.PrepareService;

import static java.util.Objects.requireNonNullElse;

@Component("prepareSimpleDelegate")
@RequiredArgsConstructor
public class PrepareSimpleDelegate implements JavaDelegate {
    private final PrepareService prepareService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var request = createServiceRequest(execution);
        var result = prepareService.prepareResponse(request);

        var responseAlias = (String) execution.getVariable("responseAlias");
        execution.setVariable(requireNonNullElse(responseAlias, "response"), result);
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
}
