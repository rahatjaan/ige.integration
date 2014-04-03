package com.integration.td.processes;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.integration.td.constants.DataSource;
import com.integration.td.constants.EmailSource;
import com.integration.td.model.TenantInfo;
import com.integration.td.utils.SendEmail;
import com.integration.td.utils.XMLElementExtractor;
import com.mysql.jdbc.Statement;

public class AddTenantProcessor implements Processor {
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
		String value = exchange.getIn().getBody().toString();
		String outboundUrl = XMLElementExtractor.extractXmlElementValue(value, "outboundUrl");
		String outboundType = XMLElementExtractor.extractXmlElementValue(value, "outboundType");
		String userName = XMLElementExtractor.extractXmlElementValue(value, "userName");
		String password = XMLElementExtractor.extractXmlElementValue(value, "password");
		String tenantGUID = XMLElementExtractor.extractXmlElementValue(value, "tenantGUID");
		if(addTenantInfo(outboundUrl,outboundType,userName, password, tenantGUID)){
			exchange.getOut().setBody("Tenant Infomation Added.");
		}else{
			exchange.getOut().setBody("Couldn't add tenant. Please see your parameters. OutboundType should be integer.");
		}
	}

	Connection connection;
	Statement statement;
	PreparedStatement preparedStatement;
	ResultSet resultSet;
	
	public boolean addTenantInfo(String outboundUrl,String outboundType,String userName, String password, String tenantGUID) throws Exception{
		//JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		boolean flag = false;
		String sql = "insert into tenant(outbound_end_point_type,outbound_url,userName, password, tenantGUID) values(?,?,?,?,?)";
		try{
			Class.forName("com.mysql.jdbc.Driver");
		      // Setup the connection with the DB
			connection = DriverManager
			          .getConnection("jdbc:mysql://"+dataSource.getHOST()+":"+dataSource.getPORT()+"/"+dataSource.getDATABASE()+"?"
			              + "user="+dataSource.getUSER()+"&password="+dataSource.getPASS());
	    	preparedStatement = connection.prepareStatement(sql);
	    	preparedStatement.setInt(1,Integer.parseInt(outboundType));
	    	preparedStatement.setString(2,outboundUrl);
	    	preparedStatement.setString(3,userName);
	    	preparedStatement.setString(4,password);
	    	preparedStatement.setString(5,tenantGUID);
	    	preparedStatement.executeUpdate();
	    	flag = true;
		}catch(Exception e){
			e.printStackTrace();
			String mesg = "AddTenantProcessor: addTenantInfo "+e.toString();
            if(1 == new SendEmail().sendEmail(emailSource.getHOST(), emailSource.getFROM_EMAIL(), emailSource.getADMIN_EMAIL(), emailSource.getPASS(), emailSource.getPORT(), null, "Database Failure at AddTenantProcessor", mesg,emailSource.getFROM_NAME())){
				//exchange.getOut().setBody("<Message><Failure>An exception has occured. An email is sent to Admin.</Failure></Message>");
			}else{
				//exchange.getOut().setBody("<Message><Failure>An exception has occured. Email sending to Admin failed too.</Failure></Message>");
			}
		}
			return flag;
	}


}
