package org.wso2.apim.monetization.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ApprovalManager {
    private static final Log log = LogFactory.getLog(ApprovalManager.class);

    static boolean isPriorApproved(String username, String wfType) throws SQLException {
        final String sqlQuery = "SELECT 1 FROM AM_ONE_TIME_APPROVALS " +
                "WHERE USER_NAME = ? AND WF_TYPE = ? AND WF_STATUS = '" + WorkflowStatus.APPROVED.name() + "'";
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            connection.setAutoCommit(false);
            connection.commit();

            statement.setString(1, username);
            statement.setString(2, wfType);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    static void recordApprovalRequest(String username, String wfType, String wfExternalRef, WorkflowStatus wfStatus) throws SQLException {
        final String sqlQuery = "INSERT INTO AM_ONE_TIME_APPROVALS (USER_NAME, WF_TYPE, WF_EXTERNAL_REFERENCE, WF_STATUS) " +
                "VALUES (?,?,?,?)";

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            connection.setAutoCommit(false);

            statement.setString(1, username);
            statement.setString(2, wfType);
            statement.setString(3, wfExternalRef);
            statement.setString(4, wfStatus.toString());

            statement.executeUpdate();

            connection.commit();
        }
    }

    static void updateApprovalStatus(String wfExternalRef, WorkflowStatus wfStatus) throws SQLException {
        final String sqlQuery = "UPDATE AM_ONE_TIME_APPROVALS SET WF_STATUS = ? " +
                "WHERE WF_EXTERNAL_REFERENCE = ?";

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            connection.setAutoCommit(false);

            statement.setString(1, wfStatus.toString());
            statement.setString(2, wfExternalRef);

            statement.executeUpdate();

            connection.commit();
        }
    }

    static String getApproveRequestUser(String wfExternalRef) throws SQLException {
        final String sqlQuery = "SELECT USER_NAME FROM AM_ONE_TIME_APPROVALS " +
                "WHERE WF_EXTERNAL_REFERENCE = ?";

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            connection.setAutoCommit(false);
            connection.commit();

            statement.setString(1, wfExternalRef);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    return rs.getString("USER_NAME");
                }
            }
        }

        return "";
    }
}
