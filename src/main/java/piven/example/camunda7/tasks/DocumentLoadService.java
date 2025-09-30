package piven.example.camunda7.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@Service("loadService")
@Slf4j
@RequiredArgsConstructor
public class DocumentLoadService implements JavaDelegate {
    private final RestTemplate restTemplate;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        getDocuments(execution);
    }

    public void getDocuments(DelegateExecution execution) {
        try {
            var clientId = (String) execution.getVariable("clientId");

            var url = "http://localhost:9090/api/documents?clientId=" + clientId;
            var response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {

                var documents = response.getBody();
                execution.setVariable("documentsLoaded", true);
                execution.setVariable("documentsData", documents);
                log.info("Документы успешно загружены для клиента: " + clientId);
            } else {
                throw new RuntimeException("Ошибка загрузки документов. HTTP статус: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке документов: " + e.getMessage(), e);
        }
    }
}