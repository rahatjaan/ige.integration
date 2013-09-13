package ige.integration.transformer;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class GuestCheckInTransformer {
	public static String transform(String message) throws ParserConfigurationException, SAXException, IOException{
		int ind1 = message.indexOf("<return>");
		int ind2 = message.indexOf("</return>");
		message = message.substring(ind1+8,ind2);
		message = "<guestCheckInResponse>"+message+"</guestCheckInResponse>";
		System.out.println("Message is: "+message);
		//String json = "<getBillInfoResponse>";*/
		/*DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
	            .newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	    Document document = docBuilder.parse(new InputSource(new ByteArrayInputStream(message.getBytes("utf-8"))));
	    String json = "{\"getBillInfoResponse\":{";
	    NodeList nodeList = document.getElementsByTagName("*");
	    for (int i = 1; i < nodeList.getLength(); i++) {
	        Node node = nodeList.item(i);
	        if (node.getNodeType() == Node.ELEMENT_NODE) {
	        	String name = node.getNodeName();
	        	if(nodeList.getLength() == i-1){
	        		json += "\""+name+"\":\""+XMLElementExtractor.extractXmlElementValue(message, name)+"\"";
	        	}else{
	        		json += "\""+name+"\":\""+XMLElementExtractor.extractXmlElementValue(message, name)+"\",";
	        	}
	            // do something with the current element
	            System.out.println(name);
	        }
	    }
	    json += "}}";*/
		/*json = "{\"BillDetails\":{";
		json += "\"FirstName\":\""+XMLElementExtractor.extractXmlElementValue(message, "FirstName")+"\",";
		json += "\"LastName\":\""+XMLElementExtractor.extractXmlElementValue(message, "LastName")+"\",";
		json += "\"MemberShipNumber\":\""+XMLElementExtractor.extractXmlElementValue(message, "MemberShipNumber")+"\",";
		json += "\"MobileNumber\":\""+XMLElementExtractor.extractXmlElementValue(message, "MobileNumber")+"\",";
		json += "\"RoomNumber\":\""+XMLElementExtractor.extractXmlElementValue(message, "RoomNumber")+"\",";
		json += "\"FloorNumber\":\""+XMLElementExtractor.extractXmlElementValue(message, "FloorNumber")+"\",";
		json += "\"FolioNumber\":\""+XMLElementExtractor.extractXmlElementValue(message, "FolioNumber")+"\",";
		json += "\"TotalBill\":\""+XMLElementExtractor.extractXmlElementValue(message, "TotalBill")+"\",";
		json += "\"CardNumber\":\""+XMLElementExtractor.extractXmlElementValue(message, "CardNumber")+"\",";
		json += "\"BalanceAmount\":\""+XMLElementExtractor.extractXmlElementValue(message, "BalanceAmount")+"\",";
		json += "\"TotalChildren\":\""+XMLElementExtractor.extractXmlElementValue(message, "TotalChildren")+"\",";
		json += "\"TotalAdult\":\""+XMLElementExtractor.extractXmlElementValue(message, "TotalAdult")+"\",";
		json += "\"RoomRate\":\""+XMLElementExtractor.extractXmlElementValue(message, "RoomRate")+"\",";
		json += "\"TransactionId\":\""+XMLElementExtractor.extractXmlElementValue(message, "TransactionId")+"\",";
		json += "\"TransactionDate\":\""+XMLElementExtractor.extractXmlElementValue(message, "TransactionDate")+"\",";
		json += "\"ReferenceNumber\":\""+XMLElementExtractor.extractXmlElementValue(message, "ReferenceNumber")+"\",";
		json += "\"Charges\":\""+XMLElementExtractor.extractXmlElementValue(message, "Charges")+"\"";
		json += "}}";*/
		return message;
	}
}
