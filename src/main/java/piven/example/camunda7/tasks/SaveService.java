package piven.example.camunda7.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import piven.example.camunda7.dto.ServiceRequest;
import piven.example.camunda7.dto.ServiceResult;

@Slf4j
@Service
public class SaveService {
    public ServiceResult save(ServiceRequest request) {
        log.info("Сохранение для requestId: {}, scenario: {}, type: {}, status: {}",
                request.getRequestId(), request.getScenario(), request.getType(), request.getStatus());

        if (request.getMapResponse() != null) {
            log.info("Дополнительные данные для сохранения: {}", request.getMapResponse());
        }

        var result = new ServiceResult();
        result.setRequestId(request.getRequestId());
        result.setScenario(request.getStatus());
        result.setScenario(request.getScenario());
        result.setType(request.getType());
        result.setSuccess(true);
        result.setMessage("Данные успешно сохранены");

        return result;
    }
}
