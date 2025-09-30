package piven.example.camunda7.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProcessVariables {
    private Boolean enabled;
    private String scenario;
    private String requestIdAliasStartPositivePrepare;
    private String responseAliasStartPositivePrepare;
    private String startPrepareTypePositive;
    private String requestIdAliasStartPositivePublication;
    private String responseAliasStartPositivePublication;
    private String startPublicationTypePositive;
    private String requestIdAliasSavePositive;
    private String responseAliasSavePositive;
    private String saveTypePositive;
    private String requestIdAliasEndPositivePrepare;
    private String responseAliasEndPositivePrepare;
    private String endPrepareTypePositive;
    private String requestIdAliasEndPositivePublication;
    private String responseAliasEndPositivePublication;
    private String endPublicationTypePositive;
    private String requestIdAliasStartNegativePrepare;
    private String responseAliasStartNegativePrepare;
    private String startPrepareTypeNegative;
    private String requestIdAliasStartNegativePublication;
    private String responseAliasStartNegativePublication;
    private String startPublicationTypeNegative;
    private String requestIdAliasSaveNegative;
    private String responseAliasSaveNegative;
    private String saveTypeNegative;
    private String requestIdAliasEndNegativePrepare;
    private String responseAliasEndNegativePrepare;
    private String endPrepareTypeNegative;
    private String requestIdAliasEndNegativePublication;
    private String responseAliasEndNegativePublication;
    private String endPublicationTypeNegative;
}
