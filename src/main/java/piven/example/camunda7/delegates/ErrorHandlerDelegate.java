package piven.example.camunda7.delegates;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("errorHandlerDelegate")
@Slf4j
public class ErrorHandlerDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        if (Boolean.TRUE.equals(execution.getVariable("errorHandled"))) {
            log.warn("Ошибка уже обработана, повторный запуск подпроцесса");
            return;
        }

        execution.setVariableLocal("errorHandled", true);

        var code = (String) execution.getVariable("loanErrorCode");
        var message = (String) execution.getVariable("loanErrorMessage");
        log.info("Обрабатываем ошибку: {} — {}", code, message);
    }
}
