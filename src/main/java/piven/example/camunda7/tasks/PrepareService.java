package piven.example.camunda7.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import piven.example.camunda7.dto.ServiceRequest;
import piven.example.camunda7.dto.ServiceResult;

@Slf4j
@Service("prepareService")
public class PrepareService {
    public ServiceResult prepareResponse(ServiceRequest request) {
        log.info("Подготовка ответа для requestId: {}, type: {}, status: {}",
                request.getRequestId(), request.getType(), request.getStatus());

        var result = new ServiceResult();
        result.setRequestId(request.getRequestId());
        result.setStatus(request.getStatus());
        result.setType(request.getType());
        result.setSuccess(true);
        result.setMessage("Ответ успешно подготовлен");

        return result;
    }
}
