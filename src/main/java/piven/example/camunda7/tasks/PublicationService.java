package piven.example.camunda7.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import piven.example.camunda7.dto.ServiceRequest;
import piven.example.camunda7.dto.ServiceResult;

@Slf4j
@Service
public class PublicationService {
    public ServiceResult publish(ServiceRequest request) {
        log.info("Публикация для requestId: {}, type: {}, status: {}",
                request.getRequestId(), request.getType(), request.getStatus());

        if (request.getMapResponse() != null) {
            log.info("Дополнительные данные для публикации: {}", request.getMapResponse());
        }

        var result = new ServiceResult();
        result.setRequestId(request.getRequestId());
        result.setStatus(request.getStatus());
        result.setType(request.getType());
        result.setSuccess(true);
        result.setMessage("Публикация выполнена успешно");

        return result;
    }
}
