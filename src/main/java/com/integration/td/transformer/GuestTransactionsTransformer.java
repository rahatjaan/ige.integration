package com.integration.td.transformer;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class GuestTransactionsTransformer {
	public static String transform(String message, boolean flag) throws ParserConfigurationException, SAXException, IOException{
		if(flag){
			if(message.contains("guestCheckoutFALSE")){
				message = "<guestInfos><Failure>"+"Already Checked Out"+"</Failure></guestInfos>";
			}else{
				int ind1 = message.indexOf("<soap:Fault");
				int ind2 = message.indexOf("</soap:Fault>");
				message = message.substring(ind1,ind2);
				ind1 = message.indexOf("<faultcode>");
				message = message.substring(ind1);
				message= "<guestInfos><ServiceError>"+message+"</ServiceError></guestInfos>";
			}
			System.out.println("Message is: "+message);
		}else{
			message = "<guestInfos><Success>"+"Successfully Checked Out"+"</Success></guestInfos>";
			System.out.println("Message is: "+message);
			//new GenerateReport().generateReport(message);
		}
		return message;
	}
}