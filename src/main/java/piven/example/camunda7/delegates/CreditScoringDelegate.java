package piven.example.camunda7.delegates;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("Task_CreditScoring")
@Slf4j
public class CreditScoringDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        execution.getVariable("clientData");
        var creditApproved = Math.random() > 0.2; // случайное решение
        execution.setVariable("creditApproved", creditApproved);
        log.debug("Кредитный скорринг: " + (creditApproved ? "Одобрено" : "Отклонено"));
    }
}
