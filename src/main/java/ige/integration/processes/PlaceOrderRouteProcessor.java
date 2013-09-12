package ige.integration.processes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class PlaceOrderRouteProcessor implements Processor{

	public void process(Exchange arg0) throws Exception {
		arg0.getOut().setBody("ORDER RECEIVED.");
    }

}
