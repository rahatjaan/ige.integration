package ige.integration.transformer;

import ige.integration.constants.EmailSource;
import ige.integration.reporting.GenerateReport;
import ige.integration.utils.SendEmail;
import ige.integration.utils.XMLElementExtractor;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
			//message = "<guestInfos>"+message+"</guestInfos>";
			//Get the transactionDate as timestamp
			message = "<guestInfos>"+getCustomMessage(message)+"</guestInfos>";
			//Ends here
			String toEmail = XMLElementExtractor.extractXmlElementValue(message, "email");
			System.out.println(message);
			//System.exit(-1);
			String fileDateName = new Date().toString();
			fileDateName = fileDateName.replaceAll(" ","-");
			//String fp = "C:\\\\Shakeel\\GitRepos\\Reports\\";
			String myFilename = emailS.getFILE_PATH()+XMLElementExtractor.extractXmlElementValue(message, "firstName")+"-"+XMLElementExtractor.extractXmlElementValue(message, "lastName")+"-"+XMLElementExtractor.extractXmlElementValue(message, "roomNumber")+"-"+fileDateName+".pdf";
			//String myFilename = fp+XMLElementExtractor.extractXmlElementValue(message, "firstName")+"-"+XMLElementExtractor.extractXmlElementValue(message, "lastName")+"-"+XMLElementExtractor.extractXmlElementValue(message, "roomNumber")+"-"+fileDateName+".pdf";
			myFilename = myFilename.replaceAll(":","");
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
	private static String getCustomMessage(String xml){
		System.out.println("XML::: "+xml);
		int ind1 = 0;
		int ind2 = xml.indexOf("<guestStayInfo");
		String guestInfo = xml.substring(ind1,ind2);
		guestInfo += "</guestInfos>";
		guestInfo = "<guestInfos>"+guestInfo;
		ind1 = xml.indexOf("<guestStayInfo");
		ind2 = xml.indexOf("<guestTransaction");
		if(ind2 < 0){
			ind2 = xml.indexOf("</guestStayInfo");
		}
		String guestStayInfo = "";
		if(ind1 > 0 && ind2 > 0){
			guestStayInfo = xml.substring(ind1,ind2);
			guestStayInfo += "</guestStayInfos>";
			ind1 = ind2;
			ind2 = xml.indexOf("</guestStay");
		}
		String newGuestTr = "";
		String guestTransactions = xml.substring(ind1,ind2);
		while(-1 != guestTransactions.indexOf("<guestTransactionses>")){
        	ind1 = guestTransactions.indexOf("<guestTransactionses>");
        	ind2 = guestTransactions.indexOf("</guestTransactionses>");
        	String v = guestTransactions.substring(ind1,ind2);
        	v += "</guestTransactionses>";
        	guestTransactions = guestTransactions.substring(ind2+5);
        	String tD = XMLElementExtractor.extractXmlElementValue(v, "transactionDate");
        	if(null != tD && !"".equalsIgnoreCase(tD.trim())){
	        	ind1 = tD.indexOf("T");
	        	ind2 = tD.indexOf("+");
	        	if(-1 != ind1 && -1 != ind2){
		        	String d = tD.substring(0,ind1)+" "+tD.substring(ind1+1,ind2);
		        	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		        	try {		        		 
		        		Date date = df.parse(d);
		        		System.out.println(date);
		        		System.out.println(date.getTime()/1000);
		        		String va = Long.toString(date.getTime()/1000);
		        		v = v.replace(tD,va);
		        	} catch (ParseException e) {
		        		e.printStackTrace();
		        	}/*
		        	System.out.println("TIMESTAMP IS: "+d);
		        	String vaaa = Timestamp.valueOf(d).toString();
		        	System.out.println("1"+vaaa);
		        	vaaa = vaaa.replaceAll(" ","");
		        	System.out.println("2"+vaaa);
		        	vaaa = vaaa.replaceAll("-","");
		        	System.out.println("3"+vaaa);
		        	//vaaa = vaaa.replaceAll(".0","");
		        	System.out.println("4"+vaaa);
		        	vaaa = vaaa.replaceAll(":","");
		        	System.out.println("5"+vaaa);
		        	System.out.println("BEFORE: "+v);
		        	v = v.replace(tD,vaaa);
		        	System.out.println("AFTER: "+v);*/
	        	}
        	}
        	newGuestTr += v;
        }
		System.out.println("FINAL: "+guestInfo+guestStayInfo+newGuestTr);
		return guestInfo+guestStayInfo+newGuestTr;
	}
}
	
