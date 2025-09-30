package piven.example.camunda7.controllers;

import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping("/admin")
public class CleanupController {

    private final ProcessEngine processEngine;

    @PostMapping("/cleanup")
    public String cleanupHistory(@RequestParam(defaultValue = "2") int olderThanDays) {
        var cutoffDate = getDateDaysAgo(olderThanDays);
        var historyService = processEngine.getHistoryService();
        var oldInstances = historyService.createHistoricProcessInstanceQuery()
                .finished()
                .finishedBefore(cutoffDate) // завершены более чем 2 дня назад
                .list();
        oldInstances.forEach(inst -> historyService.deleteHistoricProcessInstance(inst.getId()));
        return "Удалено %s завершенных процесс(а/ов), завершившихся более чем %s дн(я/ей) назад".formatted(oldInstances.size(), olderThanDays);
    }

    @PostMapping("/delete-process")
    public String deleteProcessesByBusinessKey(@RequestParam String businessKey) {
        var runtimeService = processEngine.getRuntimeService();
        var processInstances = runtimeService
                .createProcessInstanceQuery()
                .active()
                .processInstanceBusinessKey(businessKey)
                .list();

        processInstances.forEach(instance -> runtimeService.deleteProcessInstance(instance.getId(),
                "Удаление зависшего процесса с businessKey = %s".formatted(businessKey)));

        return "Удалено %s зависших активных процесс(а/ов) c businessKey = %s".formatted(processInstances.size(), businessKey);
    }

    private Date getDateDaysAgo(int days) {
        var cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -days);
        return cal.getTime();
    }
}