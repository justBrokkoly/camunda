package piven.example.camunda7;

import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.community.process_test_coverage.spring_test.platform7.ProcessEngineCoverageConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import piven.example.camunda7.tasks.TaskCreditService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.job;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("it")
@SpringBootTest
@DirtiesContext
@Import(ProcessEngineCoverageConfiguration.class)
class LoanApplicationProcessTest {

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @MockBean
    private TaskCreditService taskCreditService;

    @Test
    @Deployment(resources = {"bpmn/loanApplicationProcess.bpmn"})
    void testNewClientApproved() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("clientId", "77777");
        vars.put("isNewClient", true);

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("loanApplicationProcess", vars);
        assertNotNull(pi);

        completeTask(pi.getId(), "Task_SubmitLoanApplication");
        completeTask(pi.getId(), "Task_UploadDocuments");
        ;
        simulateDocumentsReceived(pi.getId(), "Документы получены");

        assertThat(pi).isWaitingAt("Gateway_ParallelChecks");
        executeGatewayParallel(pi.getId(), "Gateway_ParallelChecks");
        assertThat(pi).isWaitingAt("Task_BlacklistCheck", "CallActivity_ScoringParticipantProcess");
        execute(job("Task_BlacklistCheck"));
        executeSuccessScoringSubprocess(pi);
        executeGatewayParallel(pi.getId(), "Gateway_MergeChecks");
        assertThat(pi).isEnded();
    }

    @Test
    @Deployment(resources = {"bpmn/loanApplicationProcess.bpmn"})
    void testExistingClientRejected() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("clientId", "12345");
        vars.put("isNewClient", false);

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("loanApplicationProcess", vars);
        assertNotNull(pi);

        completeTask(pi.getId(), "Task_SubmitLoanApplication");

        assertThat(pi).isWaitingAt("Gateway_ParallelChecks");
        executeGatewayParallel(pi.getId(), "Gateway_ParallelChecks");
        assertThat(pi).isWaitingAt("Task_BlacklistCheck", "CallActivity_ScoringParticipantProcess");
        execute(job("Task_BlacklistCheck"));
        executeSuccessScoringSubprocess(pi);
        executeGatewayParallel(pi.getId(), "Gateway_MergeChecks");
        assertThat(pi).isEnded();
    }

    @Test
    @Deployment(resources = {
            "bpmn/loanApplicationProcess.bpmn",
            "dmn/loanApprovalDecision.dmn"
    })
    void testExistingClient_BlacklistRejection() {
        given(taskCreditService.getScoring(any())).willReturn(70);

        var pi = runtimeService.startProcessInstanceByKey("loanApplicationProcess",
                withVariables("clientId", "12345", "isNewClient", false));

        assertThat(pi).isWaitingAt("Task_SubmitLoanApplication");
        complete(task());
        assertThat(pi).isWaitingAt("Gateway_ParallelChecks");
        executeGatewayParallel(pi.getId(), "Gateway_ParallelChecks");
        assertThat(pi).isWaitingAt("Task_BlacklistCheck", "CallActivity_ScoringParticipantProcess");
        execute(job("Task_BlacklistCheck"));
        executeSuccessScoringSubprocess(pi);
        executeGatewayParallel(pi.getId(), "Gateway_MergeChecks");
        assertThat(pi).isEnded();

        var result = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId())
                .variableName("approvalResult")
                .singleResult();

        assertEquals("REJECTED_BLACKLIST", result.getValue());
    }

    @Test
    @Deployment(resources = {
            "bpmn/loanApplicationProcess.bpmn",
            "dmn/loanApprovalDecision.dmn"
    })
    void testExistingClient_Approval() {
        given(taskCreditService.getScoring(any())).willReturn(80);

        var pi = runtimeService.startProcessInstanceByKey("loanApplicationProcess",
                withVariables("clientId", "77777", "isNewClient", false));

        assertThat(pi).isWaitingAt("Task_SubmitLoanApplication");
        complete(task());
        assertThat(pi).isWaitingAt("Gateway_ParallelChecks");
        executeGatewayParallel(pi.getId(), "Gateway_ParallelChecks");
        assertThat(pi).isWaitingAt("Task_BlacklistCheck", "CallActivity_ScoringParticipantProcess");
        execute(job("Task_BlacklistCheck"));
        executeSuccessScoringSubprocess(pi);
        executeGatewayParallel(pi.getId(), "Gateway_MergeChecks");
        assertThat(pi).isEnded();

        var result = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId())
                .variableName("approvalResult")
                .singleResult();

        assertEquals("APPROVED", result.getValue());
    }

    @Test
    @Deployment(resources = {
            "bpmn/loanApplicationProcess.bpmn",
            "dmn/loanApprovalDecision.dmn"
    })
    void testExistingClient_ScoringRejection() {
        given(taskCreditService.getScoring(any())).willReturn(30);

        var pi = runtimeService.startProcessInstanceByKey("loanApplicationProcess",
                withVariables("clientId", "88888", "isNewClient", false));

        assertThat(pi).isWaitingAt("Task_SubmitLoanApplication");
        complete(task());
        assertThat(pi).isWaitingAt("Gateway_ParallelChecks");
        executeGatewayParallel(pi.getId(), "Gateway_ParallelChecks");
        assertThat(pi).isWaitingAt("Task_BlacklistCheck", "CallActivity_ScoringParticipantProcess");
        execute(job("Task_BlacklistCheck"));
        executeSuccessScoringSubprocess(pi);
        executeGatewayParallel(pi.getId(), "Gateway_MergeChecks");
        assertThat(pi).isEnded();

        var result = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId())
                .variableName("approvalResult")
                .singleResult();

        assertEquals("REJECTED_SCORING", result.getValue());
    }

    @Test
    @Deployment(resources = {
            "bpmn/loanApplicationProcess.bpmn",
            "dmn/loanApprovalDecision.dmn"
    })
    void testNewClient_BlacklistRejection() {
        given(taskCreditService.getScoring(any())).willReturn(70);
        given(taskCreditService.getBlackList(any())).willReturn(true);

        var pi = runtimeService.startProcessInstanceByKey("loanApplicationProcess",
                withVariables("clientId", "12345", "isNewClient", true));

        assertThat(pi).isWaitingAt("Task_SubmitLoanApplication");
        complete(task());
        assertThat(pi).isWaitingAt("Task_UploadDocuments");
        complete(task());
        simulateDocumentsReceived(pi.getId(), "Документы получены");
        assertThat(pi).isWaitingAt("Gateway_ParallelChecks");
        executeGatewayParallel(pi.getId(), "Gateway_ParallelChecks");
        assertThat(pi).isWaitingAt("Task_BlacklistCheck", "CallActivity_ScoringParticipantProcess");
        execute(job("Task_BlacklistCheck"));
        executeSuccessScoringSubprocess(pi);
        executeGatewayParallel(pi.getId(), "Gateway_MergeChecks");
        assertThat(pi).isEnded();

        var result = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId())
                .variableName("approvalResult")
                .singleResult();

        assertEquals("REJECTED_BLACKLIST", result.getValue());
    }

    @Test
    @Deployment(resources = {
            "bpmn/loanApplicationProcess.bpmn",
            "dmn/loanApprovalDecision.dmn"
    })
    void testNewClient_Approval() {
        given(taskCreditService.getScoring(any())).willReturn(80);
        given(taskCreditService.getBlackList(any())).willReturn(false);

        var pi = runtimeService.startProcessInstanceByKey("loanApplicationProcess",
                withVariables("clientId", "77777", "isNewClient", true));

        assertThat(pi).isWaitingAt("Task_SubmitLoanApplication");
        complete(task());
        assertThat(pi).isWaitingAt("Task_UploadDocuments");
        complete(task());
        simulateDocumentsReceived(pi.getId(), "Документы получены");
        assertThat(pi).isWaitingAt("Gateway_ParallelChecks");
        executeGatewayParallel(pi.getId(), "Gateway_ParallelChecks");
        assertThat(pi).isWaitingAt("Task_BlacklistCheck", "CallActivity_ScoringParticipantProcess");
        execute(job("Task_BlacklistCheck"));
        executeSuccessScoringSubprocess(pi);
        executeGatewayParallel(pi.getId(), "Gateway_MergeChecks");
        assertThat(pi).isEnded();

        var result = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId())
                .variableName("approvalResult")
                .singleResult();

        assertEquals("APPROVED", result.getValue());
    }

    @Test
    @Deployment(resources = {
            "bpmn/loanApplicationProcess.bpmn",
            "bpmn/scroingParticipantProcess.bpmn",
            "dmn/loanApprovalDecision.dmn"
    })
    void testNewClient_ScoringRejection() {
        given(taskCreditService.getScoring(any())).willReturn(30);
        given(taskCreditService.getBlackList(any())).willReturn(false);

        var pi = runtimeService.startProcessInstanceByKey("loanApplicationProcess",
                withVariables("clientId", "88888", "isNewClient", true));

        assertThat(pi).isWaitingAt("Task_SubmitLoanApplication");
        complete(task());
        assertThat(pi).isWaitingAt("Task_UploadDocuments");
        complete(task());
        simulateDocumentsReceived(pi.getId(), "Документы получены");
        assertThat(pi).isWaitingAt("Gateway_ParallelChecks");
        executeGatewayParallel(pi.getId(), "Gateway_ParallelChecks");
        assertThat(pi).isWaitingAt("Task_BlacklistCheck", "CallActivity_ScoringParticipantProcess");
        execute(job("Task_BlacklistCheck"));
        executeSuccessScoringSubprocess(pi);
        executeGatewayParallel(pi.getId(), "Gateway_MergeChecks");
        assertThat(pi).isEnded();

        var result = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId())
                .variableName("approvalResult")
                .singleResult();

        assertEquals("REJECTED_SCORING", result.getValue());
    }

    @Test
    @Deployment(resources = {
            "bpmn/loanApplicationProcess.bpmn",
            "dmn/loanApprovalDecision.dmn"
    })
    void testNewClient_DocumentTimeout() {
        given(taskCreditService.getScoring(any())).willReturn(80);
        given(taskCreditService.getBlackList(any())).willReturn(false);

        var pi = runtimeService.startProcessInstanceByKey("loanApplicationProcess",
                withVariables("clientId", "99999", "isNewClient", true));

        completeTask(pi.getId(), "Task_SubmitLoanApplication");
        completeTask(pi.getId(), "Task_UploadDocuments");

        var timerJob = managementService.createJobQuery()
                .processInstanceId(pi.getId())
                .timers()
                .singleResult();

        assertNotNull(timerJob, "Timer job should exist");

        managementService.executeJob(timerJob.getId());

        waitForProcessEnd(pi.getId());

        var process = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(pi.getId())
                .singleResult();

        assertNotNull(process);
        assertEquals("COMPLETED", process.getState());

        var timeoutEvent = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(pi.getId())
                .activityId("Event_ThreeDays")
                .singleResult();

        assertNotNull(timeoutEvent, "Process should go through timeout event");

        var rejectionEvent = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(pi.getId())
                .activityId("Event_NotifyRejection")
                .singleResult();

        assertNotNull(rejectionEvent, "Process should notify about rejection");
    }

    @Test
    @Deployment(resources = {
            "bpmn/loanApplicationProcess.bpmn",
            "dmn/loanApprovalDecision.dmn"
    })
    void testNewClient_blackListCheck_clientInVipList_shouldThrowBusinessError_BlacklistRejection() {
        given(taskCreditService.getScoring(any())).willReturn(70);

        var pi = runtimeService.startProcessInstanceByKey("loanApplicationProcess",
                withVariables("clientId", "666", "isNewClient", true));

        assertThat(pi).isWaitingAt("Task_SubmitLoanApplication");
        complete(task());
        assertThat(pi).isWaitingAt("Task_UploadDocuments");
        complete(task());
        simulateDocumentsReceived(pi.getId(), "Документы получены");
        assertThat(pi).isWaitingAt("Gateway_ParallelChecks");
        executeGatewayParallel(pi.getId(), "Gateway_ParallelChecks");
        assertThat(pi).isWaitingAt("Task_BlacklistCheck", "CallActivity_ScoringParticipantProcess");
        execute(job("Task_BlacklistCheck"));
        assertThat(pi).isWaitingAt("UserTask_BlacklistCheck");
        completeTask(pi.getId(), "UserTask_BlacklistCheck", Map.of("blackList", true));
        executeSuccessScoringSubprocess(pi);
        executeGatewayParallel(pi.getId(), "Gateway_MergeChecks");
        assertThat(pi).isEnded();


        var result = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId())
                .variableName("approvalResult")
                .singleResult();

        assertEquals("REJECTED_BLACKLIST", result.getValue());
    }

    @Test
    @Deployment(resources = {
            "bpmn/loanApplicationProcess.bpmn",
            "dmn/loanApprovalDecision.dmn"
    })
    void testNewClient_blackListCheck_clintInVipList_shouldThrowTechnicalError_BlacklistRejection() throws InterruptedException {
        given(taskCreditService.getScoring(any())).willReturn(70);

        var pi = runtimeService.startProcessInstanceByKey("loanApplicationProcess",
                withVariables("clientId", "1234", "isNewClient", true, "forceBlacklistError", true));

        assertThat(pi).isWaitingAt("Task_SubmitLoanApplication");
        complete(task());
        assertThat(pi).isWaitingAt("Task_UploadDocuments");
        complete(task());
        simulateDocumentsReceived(pi.getId(), "Документы получены");
        assertThat(pi).isWaitingAt("Gateway_ParallelChecks");
        executeGatewayParallel(pi.getId(), "Gateway_ParallelChecks");
        assertThat(pi).isWaitingAt("Task_BlacklistCheck", "CallActivity_ScoringParticipantProcess");
        execute(job("Task_BlacklistCheck"));
        assertThat(pi).isWaitingAt("UserTask_BlacklistCheck");
        completeTask(pi.getId(), "UserTask_BlacklistCheck", Map.of("blackList", true));
        executeSuccessScoringSubprocess(pi);
        executeGatewayParallel(pi.getId(), "Gateway_MergeChecks");
        assertThat(pi).isEnded();

        var result = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId())
                .variableName("approvalResult")
                .singleResult();

        assertEquals("REJECTED_BLACKLIST", result.getValue());
    }

    @Test
    @Deployment(resources = {
            "bpmn/loanApplicationProcess.bpmn",
            "bpmn/scroingParticipantProcess.bpmn",
            "dmn/loanApprovalDecision.dmn"
    })
    void testNewClient_ScoringLuckyApproved() {
        given(taskCreditService.getScoring(any())).willReturn(777);

        var pi = runtimeService.startProcessInstanceByKey("loanApplicationProcess",
                withVariables("clientId", "88888", "isNewClient", true,
                        "age", 18, "income", 777));

        assertThat(pi).isWaitingAt("Task_SubmitLoanApplication");
        complete(task());
        assertThat(pi).isWaitingAt("Task_UploadDocuments");
        complete(task());
        simulateDocumentsReceived(pi.getId(), "Документы получены");
        assertThat(pi).isWaitingAt("Gateway_ParallelChecks");
        executeGatewayParallel(pi.getId(), "Gateway_ParallelChecks");
        assertThat(pi).isWaitingAt("Task_BlacklistCheck", "CallActivity_ScoringParticipantProcess");
        execute(job("Task_BlacklistCheck"));
        executeSuccessScoringSubprocess(pi);
        executeGatewayParallel(pi.getId(), "Gateway_MergeChecks");
        assertThat(pi).isEnded();

        var result = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId())
                .variableName("approvalResult")
                .singleResult();

        assertEquals("LUCKY", result.getValue());
    }

    @Test
    @Deployment(resources = {
            "bpmn/loanApplicationProcess.bpmn",
            "bpmn/scroingParticipantProcess.bpmn",
            "dmn/loanApprovalDecision.dmn"
    })
    void testGetBorrowerInfoTimeout_andScoringUNDEFINED_SCORING() {

        var pi = runtimeService.startProcessInstanceByKey("loanApplicationProcess",
                withVariables("clientId", "88888", "isNewClient", true));

        assertThat(pi).isWaitingAt("Task_SubmitLoanApplication");
        complete(task());
        assertThat(pi).isWaitingAt("Task_UploadDocuments");
        complete(task());
        simulateDocumentsReceived(pi.getId(), "Документы получены");
        assertThat(pi).isWaitingAt("Gateway_ParallelChecks");
        executeGatewayParallel(pi.getId(), "Gateway_ParallelChecks");
        assertThat(pi).isWaitingAt("Task_BlacklistCheck", "CallActivity_ScoringParticipantProcess");
        execute(job("Task_BlacklistCheck"));
        execute(job("CallActivity_ScoringParticipantProcess"));
        var supProc = assertThat(pi).isActive().calledProcessInstance("scoringProcess")
                .isStarted();
        supProc.isWaitingAt("Task_GetBorrowerInfo");
        execute(supProc.job().getActual());
        //Timer
        execute(supProc.job().getActual());
        supProc.isEnded();
        executeGatewayParallel(pi.getId(), "Gateway_MergeChecks");
        assertThat(pi).isEnded();
        var result = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId())
                .variableName("approvalResult")
                .singleResult();

        assertEquals("UNDEFINED_SCORING", result.getValue());
    }

    private void executeSuccessScoringSubprocess(ProcessInstance rootProcess) {
        execute(job("CallActivity_ScoringParticipantProcess"));
        var supProc = assertThat(rootProcess).isActive().calledProcessInstance("scoringProcess")
                .isStarted();
        supProc.isWaitingAt("Task_GetBorrowerInfo");
        execute(supProc.job().getActual());

        simulateDocumentsReceived(supProc.getActual().getProcessInstanceId(), "BORROWER_INFO_RECEIVED");
        supProc.isWaitingAt("Task_CreditScoring");
        execute(supProc.job().getActual());
        supProc.isEnded();
    }


    private void executeGatewayParallel(String processId, String activityId) {
        List<Job> jobs = managementService.createJobQuery()
                .processInstanceId(processId)
                .activityId(activityId).list();
        jobs.forEach(job -> managementService.executeJob(job.getId()));
    }

    private void completeTask(String processInstanceId, String taskDefinitionKey, Map<String, Object> vars) {
        var task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey(taskDefinitionKey)
                .singleResult();
        assertNotNull(task, "Task " + taskDefinitionKey + " must exist");
        taskService.complete(task.getId(), vars);
    }

    private void completeTask(String processInstanceId, String taskDefinitionKey) {
        completeTask(processInstanceId, taskDefinitionKey, Collections.emptyMap());
    }

    private void simulateDocumentsReceived(String processInstanceId, String messageId) {
        runtimeService.createMessageCorrelation(messageId)
                .processInstanceId(processInstanceId)
                .correlate();
    }

    private void waitForProcessEnd(String processInstanceId) {
        await().atMost(15, SECONDS).until(() ->
                runtimeService.createProcessInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .singleResult() == null
        );
    }
}