package ige.integration.router;

import ige.integration.processes.DynamicRouteProcessor;
import ige.integration.processes.JMSProcessor;
import ige.integration.processes.RestProcessor;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.Main;
public class IntegrationRouteBuilder extends RouteBuilder {

	private final String HOSTNAME = "smtp.gmail.com";
	private final String PORT = "587";
	private final String PASSWORD = "rahat547";
	private final String USERNAME = "igeintegration@gmail.com";
	private final String FROM = "igeintegration@gmail.com";
	private final String TO = "rahat.jaan@gmail.com";
	
	public static void main(String[] args) throws Exception{
        new Main().run(args);
    }
	
	
	public void configure() {

		igeInroomDiningFlow();
		jmsInFlow();//test flow to receive message, mocking as POS inbound endpoint
		
		//guestCheckInFlow();
	}/*
	
	private void guestCheckInFlow() {
		from("jetty:http://localhost:8181/RestConsumer/placeOrder")
		.unmarshal().xmljson()	
		.beanRef("inRoomDiningProcessor")	
		.setHeader("OutboundUrl").simple("${in.body.tenant.outboundUrl}")
		.setHeader("CamelHttpMethod").constant("POST")
		.setHeader("Content-Type").constant("application/x-www-form-urlencoded")
		.setBody(simple("payload=${in.body}"))
		.to("http://localhost:8080/POSMockup/guestcheckin");
		
	}*/

	
	private void igeInroomDiningFlow() {
		//onException(Exception.class,IOException.class).handled(true).process(new CustomExceptionProcessor());
		//from("restlet:/placeOrder?restletMethod=POST")
		//from("direct:start")
		from("jetty:http://localhost:8181/RestConsumer/placeOrder")
		.unmarshal().xmljson()	
		.beanRef("inRoomDiningProcessor")	
		.choice()
		.when(simple("${in.body.tenant.outboundType} == '404'"))
		.beanRef("responseProcessor")
		.when(simple("${in.body.tenant.outboundType} == '1'"))
		.setHeader("OutboundUrl").simple("${in.body.tenant.outboundUrl}")
		.setHeader("CamelHttpMethod").constant("POST")
		.setHeader("Content-Type").constant("application/x-www-form-urlencoded")
		.setBody(simple("payload=${in.body}"))
		//.to("http://localhost:8080/POSMockup/InRoomDining")
		.process(new DynamicRouteProcessor())
		//.to("uri:"+simple("${in.body.tenant.outboundUrl}"))
		.when(simple("${in.body.tenant.outboundType} == '2'"))
		.setBody(this.body())
		.to("jms:orders")
		.when(simple("${in.body.tenant.outboundType} == '3'"))
		.setHeader("subject", constant("TEST"))
		.to("smtp://" + HOSTNAME + ":" + PORT + "?password=" + PASSWORD
				+ "&username=" + USERNAME + "&from=" + FROM + "&to="
				+ TO + "&mail.smtp.starttls.enable=true")
		.otherwise()
		.beanRef("responseProcessor");
	}

	private void restLetInFlow() {
		from("restlet:/postOrder?restletMethod=GET").process(
				new RestProcessor());
	}

	private void jmsInFlow() {
		from("jms:orders").process(new JMSProcessor());
	}
}
