package com.integration.td.processes;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.integration.td.constants.DataSource;
import com.integration.td.constants.EmailSource;
import com.integration.td.model.InRoomOrderPayLoad;
import com.integration.td.model.TenantInfo;
import com.integration.td.utils.SendEmail;
import com.mysql.jdbc.Statement;

public class IntegrationProcessor implements Processor {
	
	private DataSource dataSource;
	private EmailSource emailSource;
	
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
		InRoomOrderPayLoad payload = populateTenantInfo(exchange);
		exchange.getOut().setBody(payload);
		System.out.println("IntegrationProcessor \nout: "+payload.getPayload());

	}
	
	
	public InRoomOrderPayLoad populateTenantInfo(Exchange exchange){
		String value = exchange.getIn().getBody().toString();
		System.out.println("PopulateTenantInfo xml \n"+value);
		InRoomOrderPayLoad payload = new InRoomOrderPayLoad(value,getTenantInfo("test_guid"));
		System.out.println("PAYLOAD HERE IS: "+payload.getTenant().getTenantId());		
		return payload;
	}
	Connection connection;
	Statement statement;
	PreparedStatement preparedStatement;
	ResultSet resultSet;
	public TenantInfo getTenantInfo(String tenantId){
		 //JdbcTemplate jdbc = new JdbcTemplate(dataSource);
	     String sql = "select * from tenant where tenant_guid='"+tenantId+"'";
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
				String mesg = "IntegrationProcessor: getTenantInfo "+e.toString();
	            if(1 == new SendEmail().sendEmail(emailSource.getHOST(), emailSource.getFROM_EMAIL(), emailSource.getADMIN_EMAIL(), emailSource.getPASS(), emailSource.getPORT(), null, "Exception occured at IntegrationProcessor", mesg,emailSource.getFROM_NAME())){
					//exchange.getOut().setBody("<Message><Failure>An exception has occured. An email is sent to Admin.</Failure></Message>");
				}else{
					//exchange.getOut().setBody("<Message><Failure>An exception has occured. Email sending to Admin failed too.</Failure></Message>");
				}
			}
				return info;
	}
}

