package ige.integration.audit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

public class AuditTrailDAO {
	private static final org.apache.log4j.Logger LOGGER = Logger.getLogger(AuditTrailDAO.class.getName());
	
	public void addAuditTrail(final AuditLogs auditlog,final DataSource dataSource) {
        //Add Database Logic here.
    	JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    	
		String sql = "INSERT INTO auditLogs VALUES ('"+auditlog.getCreatedBy()+"','"+auditlog.getCreatedDate().toString()+"','"+auditlog.getActionDescription()+"','"+auditlog.getActionResult()+"','"+auditlog.getUserName()+"','"+auditlog.getTenantId()+"','"+auditlog.getRequestPayload()+",'"+auditlog.getResponsePayload()+"'";
		System.out.println(sql);
		
		jdbc.update(new PreparedStatementCreator()
	    {
			public PreparedStatement createPreparedStatement(Connection arg0)
					throws SQLException {
				arg0 = dataSource.getConnection();
				PreparedStatement ps = arg0.prepareStatement("insert into auditlog (createdBy, createdDate, actionDescription, actionResult, userName, tenantId, requestPayload, responsePayload) values (?,?,?,?,?,?,?,?)");
			    ps.setString(1, auditlog.getCreatedBy());
			    ps.setString(2, auditlog.getCreatedDate());
			    ps.setString(3, auditlog.getActionDescription());
			    ps.setString(4, auditlog.getActionResult());
			    ps.setString(5, auditlog.getUserName());
			    ps.setString(6, auditlog.getTenantId());
			    ps.setString(7, auditlog.getRequestPayload());
			    ps.setString(8, auditlog.getResponsePayload());
			    return ps;
			}
	    });
    }
    
    
}
