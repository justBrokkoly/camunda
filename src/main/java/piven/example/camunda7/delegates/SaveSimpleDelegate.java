package piven.example.camunda7.delegates;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piven.example.camunda7.dto.ServiceRequest;
import piven.example.camunda7.dto.ServiceResult;
import piven.example.camunda7.tasks.SaveService;

import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNullElse;

@Component("saveSimpleDelegate")
@RequiredArgsConstructor
public class SaveSimpleDelegate implements JavaDelegate {
    private final SaveService saveService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var request = createServiceRequest(execution);
        var result = saveService.save(request);

        var responseAlias = (String) execution.getVariable("responseAlias");
        execution.setVariable(requireNonNullElse(responseAlias, "response"), result);
    }

    @SuppressWarnings("unchecked")
    private ServiceRequest createServiceRequest(DelegateExecution execution) {
        ServiceRequest request = new ServiceRequest();
        request.setRequestId((String) execution.getVariable("requestId"));
        request.setResponse(execution.getVariable("response"));
        request.setScenario((String) execution.getVariable("scenario"));
        request.setStatus((String) execution.getVariable("status"));
        request.setType((String) execution.getVariable("type"));
        request.setEnabled((Boolean) execution.getVariable("enabled"));
        request.setMapResponse((Map<String, Object>) execution.getVariable("mapResponse"));
        return request;
    }
}
