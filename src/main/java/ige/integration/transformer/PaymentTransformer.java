package ige.integration.transformer;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class PaymentTransformer {
	public static String transform(String message,boolean flag) throws ParserConfigurationException, SAXException, IOException{
		if(flag){
			int ind1 = message.indexOf("<soap:Fault");
			int ind2 = message.indexOf("</soap:Fault>");
			message = message.substring(ind1,ind2);
			ind1 = message.indexOf("<faultcode>");
			message = message.substring(ind1);
			message = "<PaymentCardProcessingRS><ServiceError>"+message+"</ServiceError></PaymentCardProcessingRS>";
			System.out.println("Message is: "+message);
		}else{
			int ind1 = message.indexOf("<return");
			int ind2 = message.indexOf("</return>");
			message = message.substring(ind1+3,ind2);
			System.out.println("ROUGH: "+message);
			ind1 = message.indexOf("<");
			System.out.println("INDEX: "+ind1);
			message = message.substring(ind1);
			message = "<PaymentCardProcessingRS>"+message+"</PaymentCardProcessingRS>";
			System.out.println("Message is: "+message);
		}
		return message;
	}
}
