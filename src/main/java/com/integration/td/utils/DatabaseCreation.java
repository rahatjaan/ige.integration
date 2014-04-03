package com.integration.td.utils;

import javax.sql.DataSource;

public class DatabaseCreation {
	 /*private static final Logger LOG = (Logger) LoggerFactory
	            .getLogger(DatabaseCreation.class);*/
	    private DataSource dataSource;

	    public DataSource getDataSource() {
	        return dataSource;
	    }

	    public void setDataSource(DataSource dataSource) {
	        this.dataSource = dataSource;
	    }

	    public void create() throws Exception {
	        /*JdbcTemplate jdbc = new JdbcTemplate(dataSource);

	        String sql = "create table tenant (\n" + "  tenant_guid integer primary key,\n"
	                + "  outbound_end_point_type integer,\n" + "  outbound_url varchar(100),\n"
	                + "  PhoneNum varchar(50)\n"
	                + ");\n";
	        //sql += "insert into dataTable values (1,'rahat','isb','123')";

	        System.out.println("Creating table tenant ...");//LOG.info("Creating table dataTable ...");

	        try {
	            jdbc.execute("drop table tenant");
	        } catch (Throwable e) {
	            // ignore
	        }

	        jdbc.execute(sql);
	        sql = "insert into tenant values (1,1,'http://localhost:8080/RestConsumer/InRoomDinning','123')";
	        jdbc.execute(sql);
	        
	        sql = "create table auditlog (\n" + "  auditLogId integer primary key,\n"
	                + "  createdBy varchar(200),\n" + "  createdDate varchar(100),\n"
	                + "  actionDescription varchar(500),\n" + " actionResult varchar(300),\n"
	                + " userName varchar(100),\n" + " requestPayload text,\n"
	                + " responsePayload text,\n"
	                + ");\n";
	        jdbc.execute(sql);
	        System.out.println("Created table tenant ...");//LOG.info("... created table dataTable");
*/	    }

	    public void destroy() throws Exception {
	        /*JdbcTemplate jdbc = new JdbcTemplate(dataSource);

	        try {
	            jdbc.execute("drop table tenant");
	        } catch (Throwable e) {
	            // ignore
	        }*/
	    }
}
