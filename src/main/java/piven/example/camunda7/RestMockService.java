package piven.example.camunda7;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import piven.example.camunda7.dto.PersonInfo;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestMockService {


    public PersonInfo getPersonInfoCallApi(String clientId) {
        log.info("Simulate rest call....");
        return PersonInfo.builder()
                .age(22)
                .income(777777)
                .build();
    }
}
