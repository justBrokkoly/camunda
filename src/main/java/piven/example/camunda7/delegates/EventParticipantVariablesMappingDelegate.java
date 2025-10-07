package piven.example.camunda7.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.stereotype.Component;

@Component("eventParticipantVarMapping")
public class EventParticipantVariablesMappingDelegate implements DelegateVariableMapping {

    @Override
    public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
        String status = (String) subVariables.get("status");

        if("NEGATIVE".equals(status)){
            subVariables.putValue("requestIdAliasStartPrepare", superExecution.getVariable("requestIdAliasStartNegativePrepare"));
            subVariables.putValue("responseAliasStartPrepare", superExecution.getVariable("responseAliasStartNegativePrepare"));
            subVariables.putValue("startPrepareType", superExecution.getVariable("startPrepareTypeNegative"));
            subVariables.putValue("requestIdAliasStartPublication", superExecution.getVariable("requestIdAliasStartNegativePublication"));
            subVariables.putValue("responseAliasStartPublication", superExecution.getVariable("responseAliasStartNegativePublication"));
            subVariables.putValue("startPublicationType", superExecution.getVariable("startPublicationTypeNegative"));
            subVariables.putValue("requestIdAliasEndPrepare", superExecution.getVariable("requestIdAliasEndNegativePrepare"));
            subVariables.putValue("responseAliasEndPrepare", superExecution.getVariable("responseAliasEndNegativePrepare"));
            subVariables.putValue("endPrepareType", superExecution.getVariable("endPrepareTypeNegative"));
            subVariables.putValue("requestIdAliasEndPublication", superExecution.getVariable("requestIdAliasEndNegativePublication"));
            subVariables.putValue("responseAliasEndPublication", superExecution.getVariable("responseAliasEndNegativePublication"));
            subVariables.putValue("endPublicationType", superExecution.getVariable("endPublicationTypeNegative"));
            subVariables.putValue("requestIdAliasSave", superExecution.getVariable("requestIdAliasSaveNegative"));
            subVariables.putValue("responseAliasSave", superExecution.getVariable("responseAliasSaveNegative"));
            subVariables.putValue("saveType", superExecution.getVariable("saveTypeNegative"));

        }

        if("POSITIVE".equals(status)){
            subVariables.putValue("requestIdAliasStartPrepare", superExecution.getVariable("requestIdAliasStartPositivePrepare"));
            subVariables.putValue("responseAliasStartPrepare", superExecution.getVariable("responseAliasStartPositivePrepare"));
            subVariables.putValue("startPrepareType", superExecution.getVariable("startPrepareTypePositive"));
            subVariables.putValue("requestIdAliasStartPublication", superExecution.getVariable("requestIdAliasStartPositivePublication"));
            subVariables.putValue("responseAliasStartPublication", superExecution.getVariable("responseAliasStartPositivePublication"));
            subVariables.putValue("startPublicationType", superExecution.getVariable("startPublicationTypePositive"));
            subVariables.putValue("requestIdAliasEndPrepare", superExecution.getVariable("requestIdAliasEndPositivePrepare"));
            subVariables.putValue("responseAliasEndPrepare", superExecution.getVariable("responseAliasEndPositivePrepare"));
            subVariables.putValue("endPrepareType", superExecution.getVariable("endPrepareTypePositive"));
            subVariables.putValue("requestIdAliasEndPublication", superExecution.getVariable("requestIdAliasEndPositivePublication"));
            subVariables.putValue("responseAliasEndPublication", superExecution.getVariable("responseAliasEndPositivePublication"));
            subVariables.putValue("endPublicationType", superExecution.getVariable("endPublicationTypePositive"));
            subVariables.putValue("requestIdAliasSave", superExecution.getVariable("requestIdAliasSavePositive"));
            subVariables.putValue("responseAliasSave", superExecution.getVariable("responseAliasSavePositive"));
            subVariables.putValue("saveType", superExecution.getVariable("saveTypePositive"));
        }
    }

    @Override
    public void mapOutputVariables(DelegateExecution delegateExecution, VariableScope variableScope) {

    }

}