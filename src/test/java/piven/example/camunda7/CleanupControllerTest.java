package piven.example.camunda7;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import piven.example.camunda7.controllers.CleanupController;

import java.util.Date;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;

public class CleanupControllerTest {
    private final ProcessEngine processEngine = mock(ProcessEngine.class);
    private final HistoryService historyService = mock(HistoryService.class);
    private final RuntimeService runtimeService = mock(RuntimeService.class);
    private final HistoricProcessInstanceQuery query = mock(HistoricProcessInstanceQuery.class);
    private final ProcessInstanceQuery mockQuery = mock(ProcessInstanceQuery.class);
    private final CleanupController controller = new CleanupController(processEngine);

    @Test
    void cleanupHistory_test() {
        var instance1 = mock(HistoricProcessInstance.class);
        var instance2 = mock(HistoricProcessInstance.class);

        when(processEngine.getHistoryService()).thenReturn(historyService);
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(query);
        when(query.finished()).thenReturn(query);
        when(query.finishedBefore(any(Date.class))).thenReturn(query);
        when(query.list()).thenReturn(List.of(instance1, instance2));

        when(instance1.getId()).thenReturn("процесс1");
        when(instance2.getId()).thenReturn("процесс2");

        var result = controller.cleanupHistory(2);

        verify(processEngine.getHistoryService()).deleteHistoricProcessInstance("процесс1");
        verify(processEngine.getHistoryService()).deleteHistoricProcessInstance("процесс2");
        Assertions.assertTrue(result.contains("Удалено 2 завершенных процесс(а/ов), завершившихся более чем 2 дн(я/ей) назад"));
    }
    @Test
    void cleanupHistory_noProcesses_test() {
        when(processEngine.getHistoryService()).thenReturn(historyService);
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(query);
        when(query.finished()).thenReturn(query);
        when(query.finishedBefore(any(Date.class))).thenReturn(query);
        when(query.list()).thenReturn(emptyList());

        var result = controller.cleanupHistory(2);

        verify(processEngine.getHistoryService(), never()).deleteHistoricProcessInstance(anyString());
        Assertions.assertTrue(result.contains("Удалено 0 завершенных процесс(а/ов), завершившихся более чем 2 дн(я/ей) назад"));
    }
    @Test
    void deleteProcessesByBusinessKey_test() {
        var instance1 = mock(ProcessInstance.class);
        var instance2 = mock(ProcessInstance.class);
        var bsnKey = "0c848343-6b75-4ce4-b34e-ae9a3d657931";
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceQuery()).thenReturn(mockQuery);
        when(mockQuery.active()).thenReturn(mockQuery);
        when(mockQuery.processInstanceBusinessKey(bsnKey)).thenReturn(mockQuery);
        when(mockQuery.list()).thenReturn(List.of(instance1, instance2));

        when(instance1.getId()).thenReturn("инстанс1");
        when(instance2.getId()).thenReturn("инстанс2");

        var result = controller.deleteProcessesByBusinessKey("0c848343-6b75-4ce4-b34e-ae9a3d657931");

        verify(processEngine.getRuntimeService()).deleteProcessInstance("инстанс1", "Удаление зависшего процесса с businessKey = %s".formatted(bsnKey));
        verify(processEngine.getRuntimeService()).deleteProcessInstance("инстанс2", "Удаление зависшего процесса с businessKey = %s".formatted(bsnKey));

        Assertions.assertTrue(result.contains("Удалено 2 зависших активных процесс(а/ов) c businessKey = 0c848343-6b75-4ce4-b34e-ae9a3d657931"));
    }
}