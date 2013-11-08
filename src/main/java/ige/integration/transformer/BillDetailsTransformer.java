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
		DateFormat dfm = new SimpleDateFormat("yyyyMMddHHmm");
		while(-1 != guestTransactions.indexOf("<guestTransactionses>")){
        	ind1 = guestTransactions.indexOf("<guestTransactionses>");
        	ind2 = guestTransactions.indexOf("</guestTransactionses>");
        	String v = guestTransactions.substring(ind1,ind2);
        	v += "</guestTransactionses>";
        	guestTransactions = guestTransactions.substring(ind2+5);
        	String tD = XMLElementExtractor.extractXmlElementValue(v, "transactionDate");
        	String d = new String(tD);
        	tD=tD.replaceAll(" ","");
        	 try {
        		 tD = tD.replace("T"," ");
        		 tD = tD.replace("Z","");
        		 System.out.println("TD IS: "+tD);
        		System.out.println("V HERE IS: "+v);
				v = v.replace(d,Long.toString(timeConversion(tD)));
				System.out.println("V NOW IS: "+v);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				System.out.println("PARSING EXCEPTION");
			}
        	newGuestTr += v;
        }
		System.out.println("FINAL: "+guestInfo+guestStayInfo+newGuestTr);
		return guestInfo+guestStayInfo+newGuestTr;
	}
	static long unixtime;
	static DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
    static public long timeConversion(String time)
    {
        dfm.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));//Specify your timezone 
    try
    {
        unixtime = dfm.parse(time).getTime();  
        unixtime=unixtime/1000;
    } 
    catch (ParseException e) 
    {
        e.printStackTrace();
    }
    return unixtime;
    }
}
	
