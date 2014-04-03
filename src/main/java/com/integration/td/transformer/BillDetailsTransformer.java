package com.integration.td.transformer;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.integration.td.constants.EmailSource;
import com.integration.td.reporting.GenerateReport;
import com.integration.td.utils.SendEmail;
import com.integration.td.utils.XMLElementExtractor;

public class BillDetailsTransformer {
	
	public static String transformIGEBill(String message, boolean flag)
			throws ParserConfigurationException, SAXException, IOException {
		if (flag) {
			int ind1 = message.indexOf("<soap:Fault");
			int ind2 = message.indexOf("</soap:Fault>");
			message = message.substring(ind1, ind2);
			ind1 = message.indexOf("<faultcode>");
			message = message.substring(ind1);
			message = "<guestInfos><ServiceError>" + message
					+ "</ServiceError></guestInfos>";
		
		} else {
			try{
				int ind1 = message.indexOf("<return");
				int ind2 = message.indexOf("</return>");
				message = message.substring(ind1 + 3, ind2);
	//			System.out.println("ROUGH: " + message);
				ind1 = message.indexOf("<");
				System.out.println("INDEX: " + ind1);
				message = message.substring(ind1);
				message = "<guestInfos>"+getCustomXmlForIGE(message)+"</guestInfos>";
			}catch(Exception ex){
				ex.printStackTrace();
				message = "<guestInfos><ServiceError>No record found for provided request.</ServiceError></guestInfos>";
			}
		}
		System.out.println("Message is: " + message);
		return message;
	}

	
	public static String transform(String message, boolean flag,
			boolean sendEmail, EmailSource emailS,String request)
			throws ParserConfigurationException, SAXException, IOException {
		if (flag) {
			int ind1 = message.indexOf("<soap:Fault");
			int ind2 = message.indexOf("</soap:Fault>");
			message = message.substring(ind1, ind2);
			ind1 = message.indexOf("<faultcode>");
			message = message.substring(ind1);
			message = "<guestInfos><ServiceError>" + message
					+ "</ServiceError></guestInfos>";
			System.out.println("Message is: " + message);
		} else {
			
			String toEmail = XMLElementExtractor.extractXmlElementValue(
					request, "to");
			String content = XMLElementExtractor.extractXmlElementValue(
					request, "content");
			String subject = XMLElementExtractor.extractXmlElementValue(
					request, "subject");

			int ind1 = message.indexOf("<return");
			int ind2 = message.indexOf("</return>");
			message = message.substring(ind1 + 3, ind2);
			System.out.println("ROUGH: " + message);
			ind1 = message.indexOf("<");
			System.out.println("INDEX: " + ind1);
			message = message.substring(ind1);
			
			message = "<guestInfos>"+getCustomMessage(message)+"</guestInfos>";
					
			
			if(toEmail==null || toEmail.isEmpty())
				toEmail = XMLElementExtractor.extractXmlElementValue(
					message, "email");
			System.out.println(message);
			if (sendEmail) {
				String fileDateName = ""+new Date().getTime();
//				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String myFilename = fileDateName + ".pdf";
//				myFilename = myFilename.replaceAll(":", "");
							
				if (null == toEmail || "".equalsIgnoreCase(toEmail.trim())) {
					message = "<Email><Error>Guest Email Address is not found.</Error></Email>";
				} else {
					// First Generate Report
					if(content==null || content.isEmpty())
						content = emailS.getMESSAGE();
					if(subject==null || subject.isEmpty())
						subject = emailS.getSUBJECT();
					if (1 == new GenerateReport().generateReport(message,
							myFilename, emailS.getFILE_PATH())) {
						if (1 == new SendEmail().sendEmail(emailS.getHOST(),
								emailS.getFROM_EMAIL(), toEmail,
								emailS.getPASS(), emailS.getPORT(), myFilename,
								subject,content ,emailS.getFROM_NAME())) {
							message = "<Email><Success>Email sent with attached report.</Success></Email>";
						} else {
							message = "<Email><Error>Email can not be sent now. Please try later.</Error></Email>";
						}
						message = "<Email><Success>Email sent with attached report.</Success></Email>";
					} else {
						message = "<Report><Error>Report can not be generated now. Please try later.</Error></Report>";
					}
					
				}
				File file = new File(myFilename);

				if (file.delete()) {
					System.out.println(file.getName() + " is deleted!");
				} else {
					System.out.println("Delete operation is failed.");
				}
				System.out.println("Message is: " + message);

			}
		}
		return message;
	}

	private static String getCustomXmlForIGE(String xml) {
		System.out.println("XML::: " + xml);
		int ind1 = 0;
		int ind2 = xml.indexOf("<reservations");
		String guestInfo = xml.substring(ind1, ind2);
		guestInfo += "</guestInfos>";
		guestInfo = "<guestInfos>" + guestInfo;
		ind1 = xml.indexOf("<reservations");
		ind2 = xml.indexOf("<transactionses");
		if (ind2 < 0) {
			ind2 = xml.indexOf("</reservations");
		}
		String guestStayInfo = "";
		if (ind1 > 0 && ind2 > 0) {
			guestStayInfo = xml.substring(ind1, ind2);
//			guestStayInfo += "</reservations>";
			ind1 = ind2;
			ind2 = xml.indexOf("</reservations");
		}
		String newGuestTr = "";
		String guestTransactions = xml.substring(ind1, ind2);
		while (-1 != guestTransactions.indexOf("<transactionses>")) {
			ind1 = guestTransactions.indexOf("<transactionses>");
			ind2 = guestTransactions.indexOf("</transactionses>");
			String v = guestTransactions.substring(ind1, ind2);
			v += "</transactionses>";
			guestTransactions = guestTransactions.substring(ind2 + 5);
			String tD = XMLElementExtractor.extractXmlElementValue(v,
					"transactionDate");
			String d = new String(tD);
			tD = tD.replaceAll(" ", "");
			try {
				tD = tD.replace("T", " ");
				tD = tD.replace("Z", "");
//				System.out.println("TD IS: " + tD);
//				System.out.println("V HERE IS: " + v);
				v = v.replace(d, Long.toString(timeConversion(tD)));
				v = v.replaceAll("transactionses", "guestTransactionses");
				System.out.println("V NOW IS: " + v);
			} catch (Exception e1) {
				System.out.println("PARSING EXCEPTION");
			}
			newGuestTr += v;
		}
		guestStayInfo = guestStayInfo.replaceFirst("<reservations>", "<guestStayInfos>");
//		guestStayInfo = guestStayInfo.replaceFirst("</reservations>", "</guestStayInfos>");
		System.out.println("FINAL: " + guestInfo + guestStayInfo + newGuestTr);
		String finalXml =guestInfo + guestStayInfo +"</guestStayInfos>"+ newGuestTr; 
		return finalXml;
	}

	
	private static String getCustomMessage(String xml) {
		System.out.println("XML::: " + xml);
		int ind1 = 0;
		int ind2 = xml.indexOf("<reservations");
		String guestInfo = xml.substring(ind1, ind2);
		guestInfo += "</guestInfos>";
		guestInfo = "<guestInfos>" + guestInfo;
		ind1 = xml.indexOf("<reservations");
		ind2 = xml.indexOf("<transactionses");
		if (ind2 < 0) {
			ind2 = xml.indexOf("</reservations");
		}
		String guestStayInfo = "";
		if (ind1 > 0 && ind2 > 0) {
			guestStayInfo = xml.substring(ind1, ind2);
			guestStayInfo += "</reservations>";
			ind1 = ind2;
			ind2 = xml.indexOf("</reservations");
		}
		String newGuestTr = "";
		String guestTransactions = xml.substring(ind1, ind2);
		while (-1 != guestTransactions.indexOf("<transactionses>")) {
			ind1 = guestTransactions.indexOf("<transactionses>");
			ind2 = guestTransactions.indexOf("</transactionses>");
			String v = guestTransactions.substring(ind1, ind2);
			v += "</transactionses>";
			guestTransactions = guestTransactions.substring(ind2 + 5);
			String tD = XMLElementExtractor.extractXmlElementValue(v,
					"transactionDate");
			String d = new String(tD);
			tD = tD.replaceAll(" ", "");
			try {
				tD = tD.replace("T", " ");
				tD = tD.replace("Z", "");
				System.out.println("TD IS: " + tD);
				System.out.println("V HERE IS: " + v);
				v = v.replace(d, Long.toString(timeConversion(tD)));
				System.out.println("V NOW IS: " + v);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				System.out.println("PARSING EXCEPTION");
			}
			newGuestTr += v;
		}
		System.out.println("FINAL: " + guestInfo + guestStayInfo + newGuestTr);
		return guestInfo + guestStayInfo + newGuestTr;
	}

	static long unixtime;
	static DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static public long timeConversion(String time) {
//		dfm.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));// Specify your
															// timezone
		try {
			unixtime = dfm.parse(time).getTime();
			unixtime = unixtime / 1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return unixtime;
	}
}
