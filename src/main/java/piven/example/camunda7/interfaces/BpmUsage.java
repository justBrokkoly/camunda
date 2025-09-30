package piven.example.camunda7.interfaces;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BpmUsage {
    String reason() default "Метод для работы BPM. Удаление приведет к ошибке!";
}
