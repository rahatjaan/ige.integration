package ige.integration.service;

import ige.integration.constants.DataSource;
import ige.integration.model.TenantInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.mysql.jdbc.Statement;

public class PopulateTenantInfo {
	private DataSource dataSource;
	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
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
