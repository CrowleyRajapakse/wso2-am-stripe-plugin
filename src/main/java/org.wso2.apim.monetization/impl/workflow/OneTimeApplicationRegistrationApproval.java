package org.wso2.apim.monetization.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.workflow.ApplicationRegistrationSimpleWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.ApplicationRegistrationWSWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;

import java.sql.SQLException;

public class OneTimeApplicationRegistrationApproval extends ApplicationRegistrationWSWorkflowExecutor {
    private static final Log log = LogFactory.getLog(OneTimeApplicationRegistrationApproval.class);

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        String subscriber = ((ApplicationRegistrationWorkflowDTO) workflowDTO).getUserName();

        try {
            if (ApprovalManager.isPriorApproved(subscriber, WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION)) {
                ApplicationRegistrationSimpleWorkflowExecutor simpleExecutor = new ApplicationRegistrationSimpleWorkflowExecutor();
                return simpleExecutor.execute(workflowDTO);
            }

            ApprovalManager.recordApprovalRequest(subscriber, WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION,
                    workflowDTO.getExternalWorkflowReference(), workflowDTO.getStatus());
        } catch (SQLException e) {
            throw new WorkflowException("Error while checking if user is prior approved for subscription in DB", e);
        }

        return super.execute(workflowDTO);
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        WorkflowResponse response = super.complete(workflowDTO);

        try {
            ApprovalManager.updateApprovalStatus(workflowDTO.getExternalWorkflowReference(), workflowDTO.getStatus());
        } catch (SQLException e) {
            throw new WorkflowException("Error while recording that user is approved for subscription in DB", e);
        }

        return response;
    }
}
