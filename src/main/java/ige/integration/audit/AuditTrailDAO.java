package ige.integration.audit;

import ige.integration.constants.DataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class AuditTrailDAO {
	//private static final org.apache.log4j.Logger LOGGER = Logger.getLogger(AuditTrailDAO.class.getName());
	Connection connection;
	Statement statement;
	PreparedStatement preparedStatement;
	public void addAuditTrail(final AuditLogs auditlog, DataSource dataSource) {
        //Add Database Logic here.
    	//JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		try{
			Class.forName("com.mysql.jdbc.Driver");
		      // Setup the connection with the DB
		      connection = DriverManager
		          .getConnection("jdbc:mysql://"+dataSource.getHOST()+":"+dataSource.getPORT()+"/"+dataSource.getDATABASE()+"?"
		              + "user="+dataSource.getUSER()+"&password="+dataSource.getPASS());
		String sql = "insert into auditlog (createdBy, createdDate, actionDescription, actionResult, userName, tenantId, requestPayload, responsePayload) values (?,?,?,?,?,?,?,?)";
		System.out.println(sql);
		preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setString(1, auditlog.getCreatedBy());
		preparedStatement.setString(2, auditlog.getCreatedDate());
		preparedStatement.setString(3, auditlog.getActionDescription());
		preparedStatement.setString(4, auditlog.getActionResult());
		preparedStatement.setString(5, auditlog.getUserName());
		preparedStatement.setString(6, auditlog.getTenantId());
		preparedStatement.setString(7, auditlog.getRequestPayload());
		preparedStatement.setString(8, auditlog.getResponsePayload());
		
		preparedStatement.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}
		
    }
    
    
}
