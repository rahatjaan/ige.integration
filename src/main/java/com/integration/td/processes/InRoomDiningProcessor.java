package com.integration.td.processes;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.integration.td.audit.AuditTrail;
import com.integration.td.constants.DataSource;
import com.integration.td.constants.EmailSource;
import com.integration.td.model.InRoomOrderPayLoad;
import com.integration.td.model.TenantInfo;
import com.integration.td.utils.SendEmail;
import com.integration.td.utils.XMLElementExtractor;
import com.mysql.jdbc.Statement;

public class InRoomDiningProcessor implements Processor {
	private DataSource dataSource;
	private EmailSource emailSource;
	//private static final org.apache.log4j.Logger LOGGER = Logger.getLogger(InRoomDiningProcessor.class.getName());

	public EmailSource getEmailSource() {
		return emailSource;
	}

	public void setEmailSource(EmailSource emailSource) {
		this.emailSource = emailSource;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void process(Exchange exchange) throws Exception {
		System.out.println("Google");
		InRoomOrderPayLoad payload = populateTenantInfo(exchange);
		AuditTrail.getAuditLogInstance().updateAuditTrial("Rahat", "2013-08-25", "Testing Action", "SUCCESS", "rahatAli", "1" , exchange.getIn().getBody().toString(), exchange.getIn().getHeaders().toString(),dataSource);
		//LOGGER.info("IN ROOM DINING PROCESSOR");
		exchange.getOut().setBody(payload);
		System.out.println("IntegrationProcessor \nout: "
				+ payload.getPayload());

	}

	public InRoomOrderPayLoad populateTenantInfo(Exchange exchange) {
		try{
			System.out.println("HEREE");
			String value = exchange.getIn().getBody().toString();
			String tenantId=XMLElementExtractor.extractXmlElementValue(value, "tenantId");
			if(tenantId==null)
				tenantId = "test_guid";
			System.out.println("PopulateTenantInfo xml \n" + value+"\ntenantId:"+tenantId);
			TenantInfo tenant = getTenantInfo(tenantId);
			if(tenant==null){
				tenant = new TenantInfo();
				tenant.setOutboundType("404");
			}
			InRoomOrderPayLoad payload = new InRoomOrderPayLoad(value,tenant);
			return payload;
		}catch(Exception e){
			String mesg = "InRoomDiningProcessor: populateTenantInfo "+e.toString();
            if(1 == new SendEmail().sendEmail(emailSource.getHOST(), emailSource.getFROM_EMAIL(), emailSource.getADMIN_EMAIL(), emailSource.getPASS(), emailSource.getPORT(), null, "Exception occured at InRoomDiningProcessor", mesg,emailSource.getFROM_NAME())){
				exchange.getOut().setBody("<Message><Failure>An exception has occured. An email is sent to Admin.</Failure></Message>");
			}else{
				exchange.getOut().setBody("<Message><Failure>An exception has occured. Email sending to Admin failed too.</Failure></Message>");
			}
			exchange.getOut().setBody(e.toString());
		}
		return null;
	}

	Connection connection;
	Statement statement;
	PreparedStatement preparedStatement;
	ResultSet resultSet;
	
	public TenantInfo getTenantInfo(String tenantId) throws Exception{
		//JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		String sql = "select * from tenant where tenant_guid='" + tenantId
				+ "'";
		TenantInfo info = null;
		try{
			Class.forName("com.mysql.jdbc.Driver");
		      // Setup the connection with the DB
			connection = DriverManager
			          .getConnection("jdbc:mysql://"+dataSource.getHOST()+":"+dataSource.getPORT()+"/"+dataSource.getDATABASE()+"?"
			              + "user="+dataSource.getUSER()+"&password="+dataSource.getPASS());
	    	preparedStatement = connection.prepareStatement(sql);
	    	resultSet = preparedStatement.executeQuery();
	    	if (resultSet.next()) {
	    		info = new TenantInfo();
				info.setTenantId(resultSet
						.getString("tenant_guid"));
				info.setOutboundType(resultSet
						.getInt("outbound_end_point_type") + "");
				info.setOutboundUrl(resultSet
						.getString("outbound_url"));
				System.out.println("tenant found, outbound type:"
						+ info.getOutboundType() + ",url:"
						+ info.getOutboundUrl());
			}
		}catch(Exception e){
			e.printStackTrace();
			String mesg = "InRoomDiningProcessor: getTenantInfo "+e.toString();
            if(1 == new SendEmail().sendEmail(emailSource.getHOST(), emailSource.getFROM_EMAIL(), emailSource.getADMIN_EMAIL(), emailSource.getPASS(), emailSource.getPORT(), null, "Database Failure at InRoomDiningProcessor", mesg,emailSource.getFROM_NAME())){
				//exchange.getOut().setBody("<Message><Failure>An exception has occured. An email is sent to Admin.</Failure></Message>");
			}else{
				//exchange.getOut().setBody("<Message><Failure>An exception has occured. Email sending to Admin failed too.</Failure></Message>");
			}
		}
			return info;
	}


}
