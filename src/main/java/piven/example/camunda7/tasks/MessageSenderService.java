package piven.example.camunda7.tasks;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MessageSenderService {

    @Autowired
    private RuntimeService runtimeService;

    public void sendDocumentsReceived(String processInstanceId) {
        runtimeService.createMessageCorrelation("Документы получены")
                .processInstanceId(processInstanceId)
                .correlate();
    }

    public void sendApprovalNotification(String processInstanceId) {
        runtimeService.createMessageCorrelation("Уведомить об одобрении")
                .processInstanceId(processInstanceId)
                .correlate();
    }

    public void sendRejectionNotification(String processInstanceId) {
        runtimeService.createMessageCorrelation("Уведомить об отказе")
                .processInstanceId(processInstanceId)
                .correlate();
    }
}

