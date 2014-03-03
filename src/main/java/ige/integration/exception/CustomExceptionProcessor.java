package ige.integration.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class CustomExceptionProcessor implements Processor {
	 
    public void process(Exchange exchange) throws Exception {
//    	System.out.println("CustomExceptionProcessor Exception HERE: "+exchange.getIn().getBody(Exception.class).toString());
        //String body = "{\"ServiceError\":{\"faultstring\":\"Input parameters are either incorrect or do not match.\",\"faultreason\":\"Please verify if the number of input parameters are right and parameters are case-sensitive.\"}}";
    	String body = "{\"ServiceError\":{\"faultstring\":\"An exception has occured on the server. Email is sent to Administrator.\",\"faultreason\":\"There might be invalid request from your side or some other reason.\"}}";
        exchange.getOut().setBody(body);
    }
}
