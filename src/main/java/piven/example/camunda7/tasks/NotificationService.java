package piven.example.camunda7.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import piven.example.camunda7.interfaces.BpmUsage;

@Component("notificationService")
@Slf4j
public class NotificationService {
    @BpmUsage
    public void sendLargeAmountNotification(DelegateExecution execution) {
        var amount = (Double) execution.getVariable("amount");
        var recipient = (String) execution.getVariable("recipient");

        log.info("Отправлено уведомление о крупном платеже: " + amount + " для получателя " + recipient);
    }
}
