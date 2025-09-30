package piven.example.camunda7.monitoring;


import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("monitoringDelegate")
@RequiredArgsConstructor
public class MonitoringDelegate implements JavaDelegate {

    private final Monitoring monitoring;

    @Override
    public void execute(DelegateExecution execution) {
        String requestId = (String) execution.getVariable("requestId");
        String bsnKey = execution.getBusinessKey();
        String typeRequest = (String) execution.getVariable("typeRequest");
        String value = (String) execution.getVariable("value");

        MonDream monDream = MonDream.builder()
                .requestId(requestId)
                .bsnKey(bsnKey)
                .serviceName(typeRequest)
                .value(value)
                .build();

        monitoring.send(monDream, "Событие из Camunda Delegate");
    }
}
