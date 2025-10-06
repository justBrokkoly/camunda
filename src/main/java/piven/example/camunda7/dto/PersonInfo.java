package piven.example.camunda7.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PersonInfo {
    private Integer income;
    private Integer age;
}
