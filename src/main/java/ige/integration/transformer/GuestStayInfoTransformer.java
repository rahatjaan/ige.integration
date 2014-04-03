package ige.integration.transformer;

import ige.integration.constants.EmailSource;
import ige.integration.reporting.GenerateReport;
import ige.integration.utils.SendEmail;
import ige.integration.utils.XMLElementExtractor;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class GuestStayInfoTransformer {
	public static String transform(String message, boolean flag) throws ParserConfigurationException, SAXException, IOException{
		if(flag){
			int ind1 = message.indexOf("<soap:Fault");
			int ind2 = message.indexOf("</soap:Fault>");
			message = message.substring(ind1,ind2);
			ind1 = message.indexOf("<faultcode>");
			message = message.substring(ind1);
			message = "<GetGuestStayInfoRS><ServiceError>"+message+"</ServiceError></GetGuestStayInfoRS>";
			System.out.println("Message is: "+message);
		}else{
			int ind1 = message.indexOf("<return");
			int ind2 = message.indexOf("</return>");
			message = message.substring(ind1+3,ind2);
			System.out.println("ROUGH: "+message);
			ind1 = message.indexOf("<");
			System.out.println("INDEX: "+ind1);
			message = message.substring(ind1);
			message = "<GetGuestStayInfoRS>"+message+"</GetGuestStayInfoRS>";
			System.out.println("Message is: "+message);
		}
		return message;
	}
}
	
