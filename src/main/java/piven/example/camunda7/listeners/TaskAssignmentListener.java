package piven.example.camunda7.listeners;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

@Component("taskAssignmentListener")
public class TaskAssignmentListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String creditInspector = (String) delegateTask.getExecution().getVariable("creditInspector");

        if (creditInspector == null) {
            creditInspector = "credit_inspector_1";
            delegateTask.getExecution().setVariable("creditInspector", creditInspector);
        }

        delegateTask.setAssignee(creditInspector);
        System.out.println("Задача назначена кредитному инспектору: " + creditInspector);
    }
}
