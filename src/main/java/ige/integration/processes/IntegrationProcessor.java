package ige.integration.processes;

import ige.integration.constants.Constants;
import ige.integration.model.InRoomOrderPayLoad;
import ige.integration.model.TenantInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.mysql.jdbc.Statement;

public class IntegrationProcessor implements Processor {
	
	private Constants dataSource;
	
	public Constants getDataSource() {
		return dataSource;
	}


	public void setDataSource(Constants dataSource) {
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
			}
				return info;
	}
}

