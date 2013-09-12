package ige.integration.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class CustomExceptionProcessor implements Processor {
	 
    public void process(Exchange exchange) throws Exception {
        exchange.getOut().setBody("Could not process request.");
    }
}
