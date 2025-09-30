package piven.example.camunda7.workers;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
@Slf4j
public class AntiFraudWorker {

    @Value("${camunda.rest.url:http://localhost:9090/engine-rest}")
    private String camundaRestUrl;

//    @PostConstruct
    public void subscribeToTopics() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(camundaRestUrl)
                .asyncResponseTimeout(10000)
                .build();

        client.subscribe("anti-fraud-check")
                .lockDuration(1000)
                .handler((ExternalTask externalTask, ExternalTaskService externalTaskService) -> {

                    try {
                        // Mock вызов API антифрод-системы
                        Map<String, Object> variables = new HashMap<>();

                        // Симуляция проверки - случайный результат
                        String[] results = {"CLEAN", "FRAUD", "REVIEW"};
                        var antiFraudResult = results[new Random().nextInt(results.length)];

                        variables.put("antiFraudResult", antiFraudResult);
                        variables.put("fraudScore", new Random().nextInt(100));

                        log.info("Антифрод проверка завершена. Результат: " + antiFraudResult);

                        externalTaskService.complete(externalTask, variables);

                    } catch (Exception e) {
                        externalTaskService.handleFailure(externalTask,
                                "Ошибка антифрод проверки",
                                e.getMessage(),
                                3,
                                5000);
                    }
                })
                .open();
    }
}
