package piven.example.camunda7.delegates;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component("blacklistCheck")
@Slf4j
public class BlacklistCheckDelegate implements JavaDelegate {
    private static final String BLACKLIST_CHECK_ERROR = "BLACKLIST_CHECK_ERROR";
    private static final String TECHNICAL_ERROR = "TECHNICAL_ERROR";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var clientId = (String) execution.getVariable("clientId");

        if (Boolean.TRUE.equals(execution.getVariable("forceBlacklistError"))) {
            if (execution.getVariable("errorAlreadyThrown") == null) {
                execution.setVariable("errorAlreadyThrown", true);
                execution.setVariable("loanErrorCode", "BLACKLIST_CHECK_ERROR");
                execution.setVariable("loanErrorMessage", "Тестовая ошибка API ЧС");
                throw new BpmnError(TECHNICAL_ERROR, "Тестовая ошибка API ЧС");
            } else {
                log.warn("Ошибка уже была выброшена, повторный вызов делегата");
            }
        }

        var vipList = List.of("666","777");
        if (vipList.contains(clientId)) {
            throw new BpmnError(BLACKLIST_CHECK_ERROR, "Бизнесс ошибка client  в vipList");
        }

        try {

            var blackList = Arrays.asList("12345", "99999", "ABC001");
            var isBlackListed = blackList.contains(clientId);
            execution.setVariable("isBlackListed", isBlackListed);
            execution.setVariable("blackList", isBlackListed);

        } catch (Exception e) {
            execution.setVariable("loanErrorCode", TECHNICAL_ERROR);
            execution.setVariable("loanErrorMessage", e.getMessage());
            throw new BpmnError(TECHNICAL_ERROR,
                    "Ошибка вызова API ЧС: " + e.getMessage());
        }
    }
}