package ige.integration.router;

import ige.integration.constants.Constants;
import ige.integration.exception.CustomExceptionProcessor;
import ige.integration.processes.DynamicRouteProcessor;
import ige.integration.processes.JMSProcessor;
import ige.integration.processes.RestProcessor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
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

		onException(Exception.class,IOException.class).handled(true).process(new CustomExceptionProcessor());
		guestCheckIn();
		pmsPlaceOrder();
		igeGetBillInfo();
		jmsInFlow();//test flow to receive message, mocking as POS inbound endpoint
		//guestCheckInFlow();
	}
	
	private void guestCheckIn() {
		Map<String, String> xmlJsonOptions = new HashMap<String, String>();
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ENCODING, "UTF-8");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ROOT_NAME, "guestStayInfos");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.FORCE_TOP_LEVEL_OBJECT, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.SKIP_NAMESPACES, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.REMOVE_NAMESPACE_PREFIXES, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.EXPANDABLE_PROPERTIES, "d e");

		
		from("jetty:http://0.0.0.0:8888/guestCheckIn")
		.unmarshal().xmljson()	
		.beanRef("inRoomDiningProcessor")	
		.choice()
		.when(simple("${in.body.tenant.outboundType} == '404'"))
		.beanRef("responseProcessor")
		.when(simple("${in.body.tenant.outboundType} == '1'"))
		.setHeader("OutboundUrl").simple("${in.body.tenant.outboundUrl}")
		.setHeader("flow").constant(Constants.GUESTCHECKIN)
		.setHeader("CamelHttpMethod").constant("POST")
		.setHeader("Content-Type").constant("application/x-www-form-urlencoded")
		.setBody(simple("payload=${in.body}"))
		//.to("http://localhost:8080/POSMockup/InRoomDining")
		.process(new DynamicRouteProcessor())
		.marshal().xmljson(xmlJsonOptions)
		.process(new Processor(){
			public void process(Exchange arg0) throws Exception {
				System.out.println(arg0.getIn().getBody().toString());
			}
		})
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

	
	private void igeGetBillInfo() {
		//from("restlet:/placeOrder?restletMethod=POST")
		//from("direct:start")

		Map<String, String> xmlJsonOptions = new HashMap<String, String>();
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ENCODING, "UTF-8");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ROOT_NAME, "guestInfos");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.FORCE_TOP_LEVEL_OBJECT, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.SKIP_NAMESPACES, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.REMOVE_NAMESPACE_PREFIXES, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.EXPANDABLE_PROPERTIES, "d e");

		
		from("jetty:http://0.0.0.0:8888/getBillInfo")
		.unmarshal().xmljson()	
		.beanRef("inRoomDiningProcessor")	
		.choice()
		.when(simple("${in.body.tenant.outboundType} == '404'"))
		.beanRef("responseProcessor")
		.when(simple("${in.body.tenant.outboundType} == '1'"))
		.setHeader("OutboundUrl").simple("${in.body.tenant.outboundUrl}")
		.setHeader("flow").constant(Constants.GUESTBILLINFO)
		.setHeader("CamelHttpMethod").constant("POST")
		.setHeader("Content-Type").constant("application/x-www-form-urlencoded")
		.setBody(simple("payload=${in.body}"))
		//.to("http://localhost:8080/POSMockup/InRoomDining")
		.process(new DynamicRouteProcessor())
		.marshal().xmljson(xmlJsonOptions)
		.process(new Processor(){
			public void process(Exchange arg0) throws Exception {
				System.out.println(arg0.getIn().getBody().toString());
			}
		})
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
	
	private void pmsPlaceOrder() {
		Map<String, String> xmlJsonOptions = new HashMap<String, String>();
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ENCODING, "UTF-8");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.ROOT_NAME, "guestTransactionsInfos");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.FORCE_TOP_LEVEL_OBJECT, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.SKIP_NAMESPACES, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.REMOVE_NAMESPACE_PREFIXES, "true");
		xmlJsonOptions.put(org.apache.camel.model.dataformat.XmlJsonDataFormat.EXPANDABLE_PROPERTIES, "d e");

		
		from("jetty:http://0.0.0.0:8888/placeOrder")
		.unmarshal().xmljson()	
		.beanRef("inRoomDiningProcessor")	
		.choice()
		.when(simple("${in.body.tenant.outboundType} == '404'"))
		.beanRef("responseProcessor")
		.when(simple("${in.body.tenant.outboundType} == '1'"))
		.setHeader("OutboundUrl").simple("${in.body.tenant.outboundUrl}")
		.setHeader("flow").constant(Constants.GUESTPLACEORDER)
		.setHeader("CamelHttpMethod").constant("POST")
		.setHeader("Content-Type").constant("application/x-www-form-urlencoded")
		.setBody(simple("payload=${in.body}"))
		//.to("http://localhost:8080/POSMockup/InRoomDining")
		.process(new DynamicRouteProcessor())
		.marshal().xmljson(xmlJsonOptions)
		.process(new Processor(){
			public void process(Exchange arg0) throws Exception {
				System.out.println(arg0.getIn().getBody().toString());
			}
		})
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
	
	
}
