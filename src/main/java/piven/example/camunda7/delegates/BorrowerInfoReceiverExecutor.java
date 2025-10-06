package piven.example.camunda7.delegates;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import piven.example.camunda7.RestMockService;

@Component("borrowerInfoReceiverExecutor")
@RequiredArgsConstructor
public class BorrowerInfoReceiverExecutor implements JavaDelegate {

    private final RestMockService restMockService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (Boolean.TRUE.equals(execution.getVariable("borrowerInfoTimeout"))) {
            Thread.sleep(15000);
        }
        var clientId = (String) execution.getVariable("clientId");
        var personIfo = restMockService.getPersonInfoCallApi(clientId);
        var income = personIfo.getIncome();
        var age = personIfo.getAge();
        execution.setVariable("income", income);
        execution.setVariable("age", age);
        execution.setVariable("borrowerReceived", true);
    }
}
