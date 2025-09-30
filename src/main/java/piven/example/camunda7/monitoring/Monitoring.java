package piven.example.camunda7.monitoring;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import piven.example.camunda7.utils.BpmUtil;

/**
 * Сервис для отправки данных в мониторинг
 */
@Slf4j
@Service
public class Monitoring {

    private final boolean enable;

    public Monitoring(BpmUtil util) {
        this.enable = util.isEnableMonitoring();
    }

    /**
     * Отправка события в мониторинг
     */
    public void send(MonDream monDream, String message) {
        if (enable) {
            log.warn("{}: {}", message, monDream);
            log.debug("Отправлено сообщение в мониторинг: {}, timestamp={}", monDream, System.currentTimeMillis());
        }
    }

    /**
     * Упрощённая отправка события в мониторинг
     */
    public void send(String requestId, String bsnKey, String typeRequest, String value) {
        try {
            MonDream monDream = MonDream.builder()
                    .requestId(requestId)
                    .bsnKey(bsnKey)
                    .serviceName(typeRequest)
                    .value(value)
                    .build();

            send(monDream, "Событие в мониторинг");

        } catch (Exception e) {
            log.error("Произошла ошибка при отправке события в мониторинг requestId={}, bsnKey={}: {}",
                    requestId, bsnKey, e.getMessage(), e);
        }
    }
}