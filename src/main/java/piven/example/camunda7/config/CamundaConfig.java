package piven.example.camunda7.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan("piven.example.camunda7")
public class CamundaConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
