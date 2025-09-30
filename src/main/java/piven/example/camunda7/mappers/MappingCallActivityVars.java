package piven.example.camunda7.mappers;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Optional.ofNullable;

@Service("mappingCallActivityVars")
@Slf4j
public class MappingCallActivityVars implements DelegateVariableMapping {
    @Override
    public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
        log.info("Маппинг входящих переменных {}", superExecution.getCurrentActivityName());
        subVariables.putAll(superExecution.getVariables());
    }

    public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
        Object o = subInstance.getVariable("out");
        List<String> outList;
        if (o == null) {
            outList = new ArrayList<>();
        } else if (subInstance.getVariable("out") instanceof String) {
            outList = Arrays.stream(((String) o).replace(" ", "").split(",")).toList();
        } else {
            outList = (List) o;
        }
        for (String elem : outList) {
            //если переменная в кавычках, то это имя переменной и есть
            if (elem.startsWith("\"") || elem.startsWith("'")) {
                String elemUnWrapped = elem.replace("\"'", "").replace("'", "");
                ofNullable(subInstance.getVariableTyped(elemUnWrapped).getValue()).ifPresent(v -> superExecution.setVariable(elemUnWrapped, v));
            } else {
                ofNullable((String) subInstance.getVariable(elem)).ifPresent(v -> superExecution.setVariable(v, subInstance.getVariable(v)));
            }
        }
    }
}
