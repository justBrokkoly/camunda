package piven.example.camunda7.listeners;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component("logUserTask")
public class LogTaskListener implements TaskListener {
  
  @Override
  public void notify(DelegateTask delegateTask) {

    String assignee = delegateTask.getAssignee();
    String taskId = delegateTask.getId();
    String eventName = delegateTask.getEventName();

    if ("create".equals(eventName)) {
        log.info(">>> Task CREATED - ID: {}, Initial Assignee: {}", taskId, assignee);
    } else if ("assignment".equals(eventName)) {
        log.info(">>> Task ASSIGNED - ID: {}, New Assignee: {}", taskId, assignee);
    } else if ("complete".equals(eventName)) {
        log.info(">>> Task COMPLETED - ID: {}, Final Assignee: {}", taskId, assignee);
    }
  }
}