package ige.integration.transformer;

import ige.integration.constants.EmailSource;
import ige.integration.reporting.GenerateReport;
import ige.integration.utils.SendEmail;
import ige.integration.utils.XMLElementExtractor;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class BillDetailsTransformer {
	public static String transform(String message, boolean flag, boolean sendEmail, EmailSource emailS) throws ParserConfigurationException, SAXException, IOException{
		if(flag){
			int ind1 = message.indexOf("<soap:Fault");
			int ind2 = message.indexOf("</soap:Fault>");
			message = message.substring(ind1,ind2);
			ind1 = message.indexOf("<faultcode>");
			message = message.substring(ind1);
			message = "<guestInfos><ServiceError>"+message+"</ServiceError></guestInfos>";
			System.out.println("Message is: "+message);
		}else{
			int ind1 = message.indexOf("<return");
			int ind2 = message.indexOf("</return>");
			message = message.substring(ind1+3,ind2);
			System.out.println("ROUGH: "+message);
			ind1 = message.indexOf("<");
			System.out.println("INDEX: "+ind1);
			message = message.substring(ind1);
			message = "<guestInfos>"+message+"</guestInfos>";
			String toEmail = XMLElementExtractor.extractXmlElementValue(message, "email");
			System.out.println(message);
			//System.exit(-1);
			String fileDateName = new Date().toString();
			fileDateName = fileDateName.replaceAll(" ","-");
			String myFilename = emailS.getFILE_PATH()+XMLElementExtractor.extractXmlElementValue(message, "firstName")+"-"+XMLElementExtractor.extractXmlElementValue(message, "lastName")+"-"+XMLElementExtractor.extractXmlElementValue(message, "roomNumber")+"-"+fileDateName+".pdf";
			if(sendEmail){
				if(null == toEmail || "".equalsIgnoreCase(toEmail.trim())){
					message = "<Email><Error>Guest Email Address is not found.</Error></Email>";
				}else{
					//First Generate Report
					
					if(1 == new GenerateReport().generateReport(message,myFilename,emailS.getFILE_PATH())){
						if(1 == new SendEmail().sendEmail(emailS.getHOST(), emailS.getFROM_EMAIL(), toEmail, emailS.getPASS(), emailS.getPORT(), myFilename, emailS.getSUBJECT(), emailS.getMESSAGE())){
							message = "<Email><Success>Email sent with attached report.</Success></Email>";
						}else{
							message = "<Email><Error>Email can not be sent now. Please try later.</Error></Email>";
						}
					}else{
						message = "<Report><Error>Report can not be generated now. Please try later.</Error></Report>";
					}
				}
			}
			File file = new File(myFilename);
			 
    		if(file.delete()){
    			System.out.println(file.getName() + " is deleted!");
    		}else{
    			System.out.println("Delete operation is failed.");
    		}
			System.out.println("Message is: "+message);
			//new GenerateReport().generateReport(message);
		}
		return message;
	}
}