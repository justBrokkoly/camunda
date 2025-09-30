package piven.example.camunda7.listeners;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component("processLogger")
@Slf4j
public class ProcessLogger implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) {
        try {
            String sMap = execution.getVariables().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("\n", "\n", ""));
            log.debug("Логируем бпм переменные {} ({}): {}", execution.getCurrentActivityName(),
                    execution.getCurrentActivityId(), sMap);
        } catch (ProcessEngineException e) {
            log.info("Ошибка логирования переменных {} ({})", execution.getCurrentActivityName(),
                    execution.getCurrentActivityId(), e);
        }
    }
}
