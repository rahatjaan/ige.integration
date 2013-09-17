package ige.integration.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class CustomExceptionProcessor implements Processor {
	 
    public void process(Exchange exchange) throws Exception {
        String body = "{\"ServiceError\":{\"faultstring\":\"Input parameters are either incorrect or do not match.\",\"faultreason\":\"Please verify if the number of input parameters are right and parameters are case-sensitive.\"}}";
        exchange.getOut().setBody(body);
    }
}
