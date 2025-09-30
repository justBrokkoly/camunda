package piven.example.camunda7.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import piven.example.camunda7.interfaces.BpmUsage;

@Component("paymentService")
@Slf4j
public class PaymentService {

    @BpmUsage
    public void validatePayment(DelegateExecution execution) {
        var amount = (Double) execution.getVariable("amount");
        var currency = (String) execution.getVariable("currency");
        var recipient = (String) execution.getVariable("recipient");

        if (amount == null || amount <= 0) {
            throw new RuntimeException("Неверная сумма платежа");
        }
        if (currency == null || currency.isEmpty()) {
            throw new RuntimeException("Не указана валюта");
        }
        if (recipient == null || recipient.isEmpty()) {
            throw new RuntimeException("Не указан получатель");
        }

        execution.setVariable("validationPassed", true);
    }

    @BpmUsage
    public void processPayment(DelegateExecution execution) {
        var amount = (Double) execution.getVariable("amount");
        var recipient = (String) execution.getVariable("recipient");

        log.info("Платёж на сумму " + amount + " для получателя " + recipient + " проведён успешно");
    }

    @BpmUsage
    public void rejectPayment(DelegateExecution execution) {
        var amount = (Double) execution.getVariable("amount");
        var reason = (String) execution.getVariable("rejectionReason");

        log.info("Платёж на сумму " + amount + " заблокирован. Причина: " + reason);
    }

    @BpmUsage
    public void handleTimeout(DelegateExecution execution) {
        execution.setVariable("rejectionReason", "Таймаут ожидания документа (3 дня)");
        log.info("Таймаут ожидания документа - платёж будет отклонён");
    }
}