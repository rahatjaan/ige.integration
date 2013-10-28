package ige.integration.reporting;

import ige.integration.datasource.TransactionDataSource;
import ige.integration.utils.XMLElementExtractor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.HashMap;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

public class GenerateReport {
	public static void main(String[] args){
		String val = "<guestInfos><id>1</id><firstName>Rahat</firstName><lastName>Ali</lastName><fullAddress>Islamabad</fullAddress><mobileNumber>12345</mobileNumber><ratePlan>rateplan</ratePlan><loyaltyNumber>hhnumber</loyaltyNumber><confirmationNumber>confirmationnumber</confirmationNumber><email>rahat.jaan@gmail.com</email><membershipNumber>1</membershipNumber><bonusCode>bonuscode</bonusCode><groupName>groupname</groupName><guestStayInfos><id>1</id><roomNumber>123</roomNumber><floorNumber>32</floorNumber><arrivalDate>2013-10-12T00:00:00+05:00</arrivalDate><departureDate>2013-09-18T20:42:01.026+05:00</departureDate><folioNumber>folionumber</folioNumber><totalBill>745</totalBill><paymentType>paymenttype</paymentType><creditAmount>745</creditAmount><cardType>credittype</cardType><cardNumber>7777</cardNumber><balanceAmount>0</balanceAmount><roomType>roomtype</roomType><numberOfChildren>1</numberOfChildren><numberOfAdult>2</numberOfAdult><roomRate>59</roomRate><creditcardExpirationDate>2019-12-23T00:00:00+05:00</creditcardExpirationDate><rateCode>39</rateCode><reservationType>34</reservationType><checkedOut>true</checkedOut><guestTransactionses><id>3</id><transactionDate>2013-11-11T00:00:00+05:00</transactionDate><description>Testing1</description><referenceNumber>1</referenceNumber><transactionId>1</transactionId><charges>54</charges></guestTransactionses><guestTransactionses><id>4</id><transactionDate>2013-11-11T00:00:00+05:00</transactionDate><description>Testing2</description><referenceNumber>54</referenceNumber><transactionId>1</transactionId><charges>34</charges></guestTransactionses><guestTransactionses><id>1</id><transactionDate>2013-12-23T00:00:00+05:00</transactionDate><description>description</description><referenceNumber>referencenumber</referenceNumber><transactionId>234</transactionId><charges>234</charges></guestTransactionses><guestTransactionses><id>2</id><transactionDate>2013-11-11T00:00:00+05:00</transactionDate><description>asdf</description><referenceNumber>456</referenceNumber><transactionId>456</transactionId><charges>456</charges></guestTransactionses><guestTransactionses><id>7</id><transactionDate>2013-11-11T00:00:00+05:00</transactionDate><description>Testing1</description><referenceNumber>1</referenceNumber><transactionId>1</transactionId><charges>54</charges></guestTransactionses><guestTransactionses><id>8</id><transactionDate>2013-11-11T00:00:00+05:00</transactionDate><description>Testing2</description><referenceNumber/><transactionId>1</transactionId><charges>34</charges></guestTransactionses><guestTransactionses><id>5</id><transactionDate>2013-11-11T00:00:00+05:00</transactionDate><description>Testing1</description><referenceNumber>1</referenceNumber><transactionId>1</transactionId><charges>54</charges></guestTransactionses><guestTransactionses><id>6</id><transactionDate>2013-11-11T00:00:00+05:00</transactionDate><description>Testing2</description><referenceNumber/><transactionId>1</transactionId><charges>34</charges></guestTransactionses></guestStayInfos></guestInfos>";
		
		new GenerateReport().generateReport(val,"","");
	}
	public int generateReport(String xml, String fileName, String filePath){
		if(xml.contains("<guestTran")){
			JasperReport jasperReport = null;
			JasperPrint jasperPrint = null;
			HashMap<String,String> jasperParameter = null;
			// Load the Jasper Report
			try {
				FileInputStream fis = new FileInputStream(filePath+"BillInformation.jasper");
				BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
				jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);
				//jasperReport = JasperCompileManager.compileReport(ReportGeneration.inputPath+"\\OrdersReport.jrxml");
			} catch (Exception e2) {			
				e2.printStackTrace();
				return 0;
			}
			
			int ind1 = xml.indexOf("<guestInfo");
			int ind2 = xml.indexOf("<guestStayInfo");
			String guestInfo = xml.substring(ind1,ind2);
			guestInfo += "</guestInfos>";
			ind1 = xml.indexOf("<guestStayInfo");
			ind2 = xml.indexOf("<guestTransaction");
			String guestStayInfo = xml.substring(ind1,ind2);
			guestStayInfo += "</guestStayInfos>";
			ind1 = ind2;
			ind2 = xml.indexOf("</guestStay");
			String guestTransactions = xml.substring(ind1,ind2);
			
			jasperParameter = new HashMap<String,String>();
			String imagePath = filePath+"logo.png";
			jasperParameter.put("imagePath", imagePath);
			jasperParameter.put("tenantName", "Acenonyx");
			jasperParameter.put("tenantAddress", "56 Wellington Road, E Brunswick, NJ 088161723, US");
			jasperParameter.put("roomNumber", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "roomNumber"));
			jasperParameter.put("roomType", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "roomType"));
			jasperParameter.put("children", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "numberOfChildren"));
			jasperParameter.put("adult", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "numberOfAdult"));
			jasperParameter.put("roomRate", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "roomRate"));
			jasperParameter.put("guestName", XMLElementExtractor.extractXmlElementValue(guestInfo, "firstName")+" "+XMLElementExtractor.extractXmlElementValue(guestInfo, "lastName"));
			//Process Arrival Date and Time here
			String dateToWork = XMLElementExtractor.extractXmlElementValue(guestStayInfo, "arrivalDate");
			String arrivalDate = "";
			String arrivalTime = "";
			if(null != dateToWork){
				arrivalDate = dateToWork.substring(0,dateToWork.indexOf("T"));
				arrivalTime = dateToWork.substring(dateToWork.indexOf("T")+1);
			}
			jasperParameter.put("arrivalDate", arrivalDate);
			jasperParameter.put("arrivalTime", arrivalTime);
			//Process Departure Date and Time here
			dateToWork = XMLElementExtractor.extractXmlElementValue(guestStayInfo, "departureDate");
			String departureDate = "";
			String departureTime = "";
			if(null != dateToWork){
				departureDate = dateToWork.substring(0,dateToWork.indexOf("T"));
				departureTime = dateToWork.substring(dateToWork.indexOf("T")+1);
			}
			jasperParameter.put("departureDate", departureDate);
			jasperParameter.put("departureTime", departureTime);
			jasperParameter.put("folioNumber", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "folioNumber"));
			jasperParameter.put("cardNumber", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "cardNumber"));
			jasperParameter.put("creditAmount", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "creditAmount"));
			jasperParameter.put("authorization", "Authority-Code???");
			jasperParameter.put("tenantExtraText", "EXTRA TEXT WHATEVER TENANT WANTS!!!");
			jasperParameter.put("balance", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "balanceAmount"));
			//Create DataSource to be sent
			try {
				String cAm = XMLElementExtractor.extractXmlElementValue(guestStayInfo, "creditAmount");
				jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new JRTableModelDataSource(new TransactionDataSource(guestTransactions,cAm,departureDate)));
	//			JasperFillManager.fillRep
				//jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new com.ReportEngine.Model.DataSource().establishConnection());
				JasperExportManager.exportReportToPdfFile(jasperPrint, fileName);
			} catch (JRException e1) {
				e1.printStackTrace();
				return 0;
			} catch(Exception e){
				e.printStackTrace();
				return 0;
			}
			return 1;
		}else{
			JasperReport jasperReport = null;
			JasperPrint jasperPrint = null;
			HashMap<String,String> jasperParameter = null;
			// Load the Jasper Report
			try {
				FileInputStream fis = new FileInputStream(filePath+"BillInformation.jasper");
				BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
				jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);
				//jasperReport = JasperCompileManager.compileReport(ReportGeneration.inputPath+"\\OrdersReport.jrxml");
			} catch (Exception e2) {			
				e2.printStackTrace();
				return 0;
			}
			
			int ind1 = xml.indexOf("<guestInfo");
			int ind2 = xml.indexOf("<guestStayInfo");
			String guestInfo = xml.substring(ind1,ind2);
			guestInfo += "</guestInfos>";
			ind1 = xml.indexOf("<guestStayInfo");
			ind2 = xml.indexOf("</guestStayInfo");
			String guestStayInfo = xml.substring(ind1,ind2);
			guestStayInfo += "</guestStayInfos>";
			
			jasperParameter = new HashMap<String,String>();
			String imagePath = filePath+"logo.png";
			jasperParameter.put("imagePath", imagePath);
			jasperParameter.put("tenantName", "TEMPORARY TENANT NAME");
			jasperParameter.put("tenantAddress", "TEMPORARY TENANT ADDRESS. SHOULD BE STORED IN DATABASE TO RETRIEVE");
			jasperParameter.put("roomNumber", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "roomNumber"));
			jasperParameter.put("roomType", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "roomType"));
			jasperParameter.put("children", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "numberOfChildren"));
			jasperParameter.put("adult", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "numberOfAdult"));
			jasperParameter.put("roomRate", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "roomRate"));
			jasperParameter.put("guestName", XMLElementExtractor.extractXmlElementValue(guestInfo, "firstName")+" "+XMLElementExtractor.extractXmlElementValue(guestInfo, "lastName"));
			//Process Arrival Date and Time here
			String dateToWork = XMLElementExtractor.extractXmlElementValue(guestStayInfo, "arrivalDate");
			String arrivalDate = "";
			String arrivalTime = "";
			if(null != dateToWork){
				arrivalDate = dateToWork.substring(0,dateToWork.indexOf("T"));
				arrivalTime = dateToWork.substring(dateToWork.indexOf("T")+1);
			}
			jasperParameter.put("arrivalDate", arrivalDate);
			jasperParameter.put("arrivalTime", arrivalTime);
			//Process Departure Date and Time here
			dateToWork = XMLElementExtractor.extractXmlElementValue(guestStayInfo, "departureDate");
			String departureDate = "";
			String departureTime = "";
			if(null != dateToWork){
				departureDate = dateToWork.substring(0,dateToWork.indexOf("T"));
				departureTime = dateToWork.substring(dateToWork.indexOf("T")+1);
			}
			jasperParameter.put("departureDate", departureDate);
			jasperParameter.put("departureTime", departureTime);
			jasperParameter.put("folioNumber", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "folioNumber"));
			jasperParameter.put("cardNumber", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "cardNumber"));
			jasperParameter.put("creditAmount", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "creditAmount"));
			jasperParameter.put("authorization", "AUTHORIZATION???");
			jasperParameter.put("tenantExtraText", "EXTRA TEXT WHATEVER TENANT WANTS!!!");
			jasperParameter.put("balance", XMLElementExtractor.extractXmlElementValue(guestStayInfo, "balanceAmount"));
			String guestTransactions = "";
			//Create DataSource to be sent
			try {
				String cAm = XMLElementExtractor.extractXmlElementValue(guestStayInfo, "creditAmount");
				jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new JRTableModelDataSource(new TransactionDataSource(guestTransactions,cAm,departureDate)));
	//			JasperFillManager.fillRep
				//jasperPrint = JasperFillManager.fillReport(jasperReport,jasperParameter, new com.ReportEngine.Model.DataSource().establishConnection());
				JasperExportManager.exportReportToPdfFile(jasperPrint, fileName);
			} catch (JRException e1) {
				e1.printStackTrace();
				return 0;
			} catch(Exception e){
				e.printStackTrace();
				return 0;
			}
			return 1;
		}
	}
}
