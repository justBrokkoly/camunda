package piven.example.camunda7;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.camunda.community.process_test_coverage.spring_test.platform7.ProcessEngineCoverageConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DirtiesContext
@Import(ProcessEngineCoverageConfiguration.class)
class PaymentProcessTest {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ExternalTaskService externalTaskService;
    @Autowired
    private ManagementService managementService;

    @Autowired
    private HistoryService historyService;

    @Test
    @Deployment(resources = {
            "bpmn/paymentProcess.bpmn",
            "dmn/paymentDecision.dmn"
    })
    void testHappyPath_EventGateway() throws InterruptedException {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(
                "paymentProcess",
                Map.of("amount", 200.0,
                        "currency", "RUB",
                        "recipient", "Piven S.O.",
                        "correlationKeyConfirmed", true
                )
        );

        var tasks = externalTaskService.fetchAndLock(1, "test-worker")
                .topic("anti-fraud-check", 1000L)
                .execute();

        assertThat(tasks).hasSize(1);

        externalTaskService.complete(tasks.get(0).getId(), "test-worker",
                Map.of("antiFraudResult", "CLEAN", "fraudScore", 10));

        Thread.sleep(5000);
        var supProc = BpmnAwareTests.assertThat(pi).isActive().calledProcessInstance("paymentHandling")
                .isStarted();
        runtimeService.createMessageCorrelation("Документ получен")
                .processInstanceId(supProc.getActual().getId())
                .correlate();

        await().atMost(5, TimeUnit.SECONDS).until(() ->
                historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(supProc.getActual().getId())
                        .activityId("BusinessRuleTask_Decision")
                        .finished()
                        .singleResult() != null
        );

        var userTask = taskService.createTaskQuery()
                .processInstanceId(supProc.getActual().getId())
                .singleResult();

        assertThat(userTask.getName()).isEqualTo("Проведение платежа");
        taskService.complete(userTask.getId());

        assertThat(runtimeService.createProcessInstanceQuery()
                .processInstanceId(pi.getId())
                .singleResult()).isNull();
    }

    @Test
    @Deployment(resources = {
            "bpmn/paymentProcess.bpmn",
            "dmn/paymentDecision.dmn"
    })
    void testRejectByDecision() throws InterruptedException {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(
                "paymentProcess",
                Map.of("amount", 5000.0, "currency", "RUB", "recipient", "Piven S.O.", "correlationKeyConfirmed", true)
        );

        LockedExternalTask antiFraud;
        do {
            var tasks = externalTaskService.fetchAndLock(1, "test-worker")
                    .topic("anti-fraud-check", 1000L)
                    .execute();
            if (!tasks.isEmpty()) {
                antiFraud = tasks.get(0);
                break;
            }
        } while (true);

        externalTaskService.complete(antiFraud.getId(), "test-worker",
                Map.of("antiFraudResult", "FRAUD", "fraudScore", 90));

        Thread.sleep(4000);
        var supProc = BpmnAwareTests.assertThat(pi).isActive().calledProcessInstance("paymentHandling")
                .isStarted();
        runtimeService.createMessageCorrelation("Документ получен")
                .processInstanceId(supProc.getActual().getId())
                .correlate();

        LockedExternalTask blockPayment;
        do {
            var tasks = externalTaskService.fetchAndLock(1, "test-worker")
                    .topic("block-payment", 1000L)
                    .execute();
            if (!tasks.isEmpty()) {
                blockPayment = tasks.get(0);
                break;
            }
        } while (true);

        externalTaskService.complete(blockPayment.getId(), "test-worker");


        assertThat(runtimeService.createProcessInstanceQuery()
                .processInstanceId(pi.getId()).singleResult()).isNull();
    }

    @Test
    @Deployment(resources = {
            "bpmn/paymentProcess.bpmn",
            "dmn/paymentDecision.dmn"
    })
    void testTimeout() throws InterruptedException {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(
                "paymentProcess",
                Map.of("amount", 1500.0, "currency", "RUB", "recipient", "Piven S.O.", "correlationKeyConfirmed", true)
        );

        LockedExternalTask antiFraud;
        do {
            var tasks = externalTaskService.fetchAndLock(1, "test-worker")
                    .topic("anti-fraud-check", 1000L)
                    .execute();
            if (!tasks.isEmpty()) {
                antiFraud = tasks.get(0);
                break;
            }
        } while (true);

        externalTaskService.complete(antiFraud.getId(), "test-worker",
                Map.of("antiFraudResult", "CLEAN", "fraudScore", 20));

        Thread.sleep(4000);
        var supProc = BpmnAwareTests.assertThat(pi).isActive().calledProcessInstance("paymentHandling")
                .isStarted();
        Job timerJob = managementService.createJobQuery()
                .processInstanceId(supProc.getActual().getId())
                .timers()
                .activityId("TimerEvent_3Days")
                .singleResult();

        assertNotNull(timerJob, "Timer job should exist");

        managementService.executeJob(timerJob.getId());

        var timeoutEvent = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(supProc.getActual().getId())
                .activityId("TimerEvent_3Days")
                .singleResult();

        assertNotNull(timeoutEvent, "Process should go through timeout event");

        LockedExternalTask blockPayment;
        do {
            var tasks = externalTaskService.fetchAndLock(1, "test-worker")
                    .topic("block-payment", 1000L)
                    .execute();
            if (!tasks.isEmpty()) {
                blockPayment = tasks.get(0);
                break;
            }
        } while (true);

        externalTaskService.complete(blockPayment.getId(), "test-worker");

        await().atMost(30, SECONDS).until(() ->
                runtimeService.createProcessInstanceQuery()
                        .processInstanceId(pi.getId())
                        .singleResult() == null
        );

        var reason = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(supProc.getActual().getId())
                .variableName("rejectionReason")
                .singleResult();

        assertThat(reason.getValue()).isEqualTo("Таймаут ожидания документа (3 дня)");

    }

    @Test
    @Deployment(resources = {
            "bpmn/paymentProcess.bpmn",
            "dmn/paymentDecision.dmn"
    })
    void testSmallAmount_NoNotification() throws InterruptedException {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(
                "paymentProcess",
                Map.of("amount", 50.0, "currency", "RUB", "recipient", "Piven S.O.", "correlationKeyConfirmed", false)
        );

        LockedExternalTask antiFraud;
        do {
            var tasks = externalTaskService.fetchAndLock(1, "test-worker")
                    .topic("anti-fraud-check", 20000L)
                    .execute();
            if (!tasks.isEmpty()) {
                antiFraud = tasks.get(0);
                break;
            }
        } while (true);

        externalTaskService.complete(antiFraud.getId(), "test-worker",
                Map.of("antiFraudResult", "CLEAN", "fraudScore", 1));

        Thread.sleep(4000);
        var supProc = BpmnAwareTests.assertThat(pi).isActive().calledProcessInstance("paymentHandling")
                .isStarted();
        runtimeService.createMessageCorrelation("Документ получен")
                .processInstanceId(supProc.getActual().getId())
                .correlate();

        Task userTask = taskService.createTaskQuery().processInstanceId(supProc.getActual().getId()).singleResult();
        assertThat(userTask.getName()).isEqualTo("Проведение платежа");
        taskService.complete(userTask.getId());

        assertThat(runtimeService.createProcessInstanceQuery()
                .processInstanceId(pi.getId()).singleResult()).isNull();
    }

    @Test
    @Deployment(resources = {
            "bpmn/paymentProcess.bpmn",
            "dmn/paymentDecision.dmn"
    })
    void testPaymentMessageDeclined() throws InterruptedException {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(
                "paymentProcess",
                Map.of("amount", 1500.0, "currency", "RUB", "recipient", "Piven S.O.", "correlationKeyConfirmed", true)
        );

        LockedExternalTask antiFraud;
        do {
            var tasks = externalTaskService.fetchAndLock(1, "test-worker")
                    .topic("anti-fraud-check", 10000L)
                    .execute();
            if (!tasks.isEmpty()) {
                antiFraud = tasks.get(0);
                break;
            }
        } while (true);

        externalTaskService.complete(antiFraud.getId(), "test-worker",
                Map.of("antiFraudResult", "CLEAN", "fraudScore", 20));

        Thread.sleep(4000);
        var supProc = BpmnAwareTests.assertThat(pi).isActive().calledProcessInstance("paymentHandling");
        runtimeService.createMessageCorrelation("Отмена платежа")
                .processInstanceId(supProc.getActual().getId())
                .correlate();


        await().atMost(10, SECONDS).until(() ->
                runtimeService.createProcessInstanceQuery()
                        .processInstanceId(pi.getId())
                        .singleResult() == null
        );

        assertThat(runtimeService.createProcessInstanceQuery()
                .processInstanceId(pi.getId())
                .singleResult()).isNull();
    }

    private void waitForProcessEnd(String processInstanceId) {
        await().atMost(15, SECONDS).until(() ->
                runtimeService.createProcessInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .singleResult() == null
        );
    }
}

