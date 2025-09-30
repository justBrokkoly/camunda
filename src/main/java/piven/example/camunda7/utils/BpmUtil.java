package piven.example.camunda7.utils;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;
import piven.example.camunda7.interfaces.BpmUsage;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("bpmUtil")
@Slf4j
public class BpmUtil {
    @BpmUsage
    public void throwBpmError(String errorCode, String errorMessage) {
        log.info("Throw new BPM error");
        throw new BpmnError(errorCode, errorMessage);
    }

    @BpmUsage
    public String timeoutMessageCreate(DelegateExecution execution, String message, String name, String timeout) {
        String retryCounter = String.valueOf(execution.getVariables().getOrDefault("retryCounter", 0));
        String retryTimeout = String.valueOf(execution.getVariables().getOrDefault("retryTimeout", 0));
        String retryResponseTimeout = String.valueOf(execution.getVariables().getOrDefault("retryResponseTimeout", 0));
        String sendingRetryCounter = String.valueOf(execution.getVariables().getOrDefault("sendingRetryCounter", 0));
        String sendingRetryTimeout = String.valueOf(execution.getVariables().getOrDefault("sendingRetryTimeout", 0));
        String retries = "";
        String sendingRetries = "";
        String response;
        if (retryCounter != null && !retryCounter.equals("0")) {
            retries = " репроц.:%s инт-л:%sc".formatted(retryCounter, retryTimeout);
        }
        if (sendingRetryCounter != null && !sendingRetryCounter.equals("0")) {
            sendingRetries = " вн.репроц.:%s инт-л:%c".formatted(sendingRetryCounter, sendingRetryTimeout);
        }
        if (!message.isEmpty()) {
            message = " ".concat(message).trim();
        } else {
            message = "";
        }
        response = "Превышено время ожидания ответа от сервиса %s (таймаут-лаг:%sc(1:%sc)%s%s). %s".formatted(name, timeout, retryResponseTimeout, retries, sendingRetries, message).trim();
        return response;
    }

    @BpmUsage
    public long getTimeout() {
        return TimeUnit.MINUTES.toMillis(5); // 5 минут timeout
    }

    @BpmUsage
    public long getRetryTimeout() {
        return TimeUnit.SECONDS.toMillis(30); // 30 секунд между попытками
    }

    @BpmUsage
    public long getRetryResponseTimeout() {
        return TimeUnit.MINUTES.toMillis(2); // 2 минуты timeout ответа
    }

    @BpmUsage
    public int getRetryCount() {
        return 3; // 3 попытки
    }

    public boolean isEnableMonitoring() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    @BpmUsage
    public Boolean isNegative(DelegateExecution execution) {
        String scenario = (String) execution.getVariable("scenario");
        return "NEGATIVE".equals(scenario);
    }

    @BpmUsage
    public Boolean isPositive(DelegateExecution execution) {
        String scenario = (String) execution.getVariable("scenario");
        return "POSITIVE".equals(scenario);
    }

    @BpmUsage
    public Boolean isNatural(DelegateExecution execution) {
        String scenario = (String) execution.getVariable("scenario");
        return "NATURAL".equals(scenario);
    }
}
