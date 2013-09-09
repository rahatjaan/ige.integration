package ige.integration.processes;

import ige.integration.audit.AuditTrail;
import ige.integration.constants.Constants;
import ige.integration.model.InRoomOrderPayLoad;
import ige.integration.model.TenantInfo;
import ige.integration.utils.XMLElementExtractor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.mysql.jdbc.Statement;

public class InRoomDiningProcessor implements Processor {
	private Constants dataSource;
	//private static final org.apache.log4j.Logger LOGGER = Logger.getLogger(InRoomDiningProcessor.class.getName());

	public Constants getDataSource() {
		return dataSource;
	}

	public void setDataSource(Constants dataSource) {
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
		}
			return info;
	}


}
