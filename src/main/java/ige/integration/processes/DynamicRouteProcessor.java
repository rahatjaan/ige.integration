package ige.integration.processes;

import ige.integration.constants.Constants;
import ige.integration.constants.EmailSource;
import ige.integration.transformer.BillDetailsTransformer;
import ige.integration.transformer.GuestCheckInTransformer;
import ige.integration.transformer.GuestPlaceOrderTransformer;
import ige.integration.transformer.GuestTransactionsTransformer;
import ige.integration.transformer.HotelFolioTransformer;
import ige.integration.transformer.ReservationTransformer;
import ige.integration.utils.SendEmail;
import ige.integration.utils.XMLElementExtractor;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class DynamicRouteProcessor implements Processor{

	private EmailSource emailSource;
	
	public EmailSource getEmailSource() {
		return emailSource;
	}


	public void setEmailSource(EmailSource emailSource) {
		this.emailSource = emailSource;
	}


	public void process(Exchange arg0) throws Exception {
		// TODO Auto-generated method stub
		String url = arg0.getIn().getHeader("OutboundUrl").toString();
		String flow = arg0.getIn().getHeader("flow").toString();
		System.out.println("URL IS: "+url);
		String req = arg0.getIn().getBody().toString();
		boolean isNotValidResLookUp = false;
		//***** Find whether the given host is qualified or not
		//boolean isCon = isConnectable(url);
		//*****************************************************
		//*******************************************
		if(isConnectable(url) || flow.equalsIgnoreCase(Constants.GUESTCHECKIN)){
			System.out.println(req);
			int ind1 = req.indexOf("<");
			req = req.substring(ind1);
			if(req.contains("&gt;")){
				req = req.replaceAll("&gt;",">");
				req = req.replaceAll("&lt;","<");
			}
			System.out.println("1"+req);
			req = req.substring(req.indexOf("<o>"));
			System.out.println("2"+req);
			req = req.replaceAll("<o>","");
			req = req.replaceAll("</o>","");
			req = req.replaceAll("<item>","");
			req = req.replaceAll("</item>","");
			req = req.replaceAll("<e>","<item>");
			req = req.replaceAll("</e>","</item>");
			System.out.println("3"+req);
			if(req.contains("</payload>"))
				req = req.substring(0,req.indexOf("</payload>"));
			
			System.out.println("REQUEST : "+req);
			try {
	            // Create SOAP Connection
	            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
	            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
	
	            // Send SOAP Message to SOAP Server
	            //String url = "http://ws.cdyne.com/emailverify/Emailvernotestemail.asmx";
	            SOAPMessage soapResponse = null;
	            if(Constants.GUESTBILLINFO.equalsIgnoreCase(flow) || Constants.SENDEMAIL.equalsIgnoreCase(flow)){
	            	soapResponse = soapConnection.call(createSOAPRequestForGuestBillInfo(req), url);
	            }else if(Constants.GUESTCHECKIN.equalsIgnoreCase(flow)){
	            	//soapResponse = soapConnection.call(createSOAPRequestForGuestCheckIn(req), url);
	            	// Call rest web service with following parameters firstname, lastname, email, checkout date, checkout time, email address, guest reward #, phone, and Tenant ID
	            	//String urLocator = "jetty:http://50.31.1.63:8080/RestIGEBackEnd/ws/restservice/guestCheckin";// REST URL here
	            	String urLocator = "jetty:http://50.31.1.23/ige-onpremise/api/ige/guestCheckIn.json";// REST URL here
	            	
	            	String tD = XMLElementExtractor.extractXmlElementValue(req, "departureDate");
	            	System.out.println("TD IS: "+tD);
	            	String value = "";
	            	if(null != tD && !"".equalsIgnoreCase(tD.trim())){
	    	        	ind1 = tD.indexOf("T");
	    	        	int ind2 = tD.indexOf("+");
	    	        	if(-1 != ind1 && -1 != ind2){
	    	        		String d = tD.substring(0,ind1)+" "+tD.substring(ind1+1,ind2);
	    		        	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    		        	try {		        		 
	    		        		Date date = df.parse(d);
	    		        		System.out.println(date);
	    		        		System.out.println(date.getTime()/1000);
	    		        		value = Long.toString(date.getTime()/1000);
	    		        	} catch (ParseException e) {
	    		        		e.printStackTrace();
	    		        	}
	    		        	/*String d = tD.substring(0,ind1)+" "+tD.substring(ind1+1,ind2);
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
	    		        	value = vaaa;*/
	    	        	}
	    	        	value = value.replaceAll("-","");
	            	}
	            	
	            	//Convert body to json and add in body.
	            	String jsonString = "{ 'tenant_id'='1', 'first_name'= '"+XMLElementExtractor.extractXmlElementValue(req, "firstName")+"',    'last_name'= '"+XMLElementExtractor.extractXmlElementValue(req, "lastName")+"',   'email'= '"+XMLElementExtractor.extractXmlElementValue(req, "email")+"', 'phone'= '"+XMLElementExtractor.extractXmlElementValue(req, "mobileNumber")+"',   'guest_reward_number'= '123',    'checkout_time'= '"+value+"'}";
	            	System.out.println(jsonString);
	            	arg0.getOut().setHeader(Exchange.HTTP_METHOD, "POST");
	            	arg0.getOut().setBody(jsonString);
	            	String val = arg0.getContext().createProducerTemplate().requestBody(urLocator,jsonString, String.class);
	            	System.out.println("RESPONSE IS: "+val);
	            	// Convert response into XML
	            	String xmL = "<guestCheckIn><message>"+val+"</message></guestCheckIn>";
	            	arg0.getOut().setBody(xmL);
	            	//System.out.println("here");
	            }else if(Constants.GUESTPLACEORDER.equalsIgnoreCase(flow)){
	            	soapResponse = soapConnection.call(createSOAPRequestForPlaceOrder(req), url);
	            }else if(Constants.GUESTCHECKOUT.equalsIgnoreCase(flow)){
	            	soapResponse = soapConnection.call(createSOAPRequestForGuestCheckOut(req), url);
	            }else if(Constants.RESERVLOOKUP.equalsIgnoreCase(flow)){
	            	soapResponse = soapConnection.call(createSOAPRequestForReservationLookup(req), url);
	            	if(null == soapResponse){
	            		isNotValidResLookUp = true;
	            	}
	            }else if(Constants.HOTELFOLIO.equalsIgnoreCase(flow)){
	            	soapResponse = soapConnection.call(createSOAPRequestForHotelFolio(req), url);
	            	if(null == soapResponse){
	            		isNotValidResLookUp = true;
	            	}
	            }
	            if(!Constants.GUESTCHECKIN.equalsIgnoreCase(flow) && !isNotValidResLookUp){
	            // Process the SOAP Response
	            String message = printSOAPResponse(soapResponse);
	            message = message.replaceAll("&lt;","<");
	            message = message.replaceAll("&gt;",">");
	            System.out.println("BEFORE TRANSFORMATION: "+message);
	            boolean flag = false;
	            if(message.contains("<faultcode>")){
	            	flag = true;
	            }
	            String body = "";
	            if(Constants.GUESTBILLINFO.equalsIgnoreCase(flow) || Constants.SENDEMAIL.equalsIgnoreCase(flow)){
	            	boolean sendEmail = false;
	            	if(Constants.SENDEMAIL.equalsIgnoreCase(flow)){
	            		sendEmail = true;
	            	}
	            	body = BillDetailsTransformer.transform(message,flag,sendEmail,emailSource);
	            }else if(Constants.GUESTCHECKIN.equalsIgnoreCase(flow)){
	            	body = GuestCheckInTransformer.transform(message,flag);
	            }else if(Constants.GUESTPLACEORDER.equalsIgnoreCase(flow)){
	            	body = GuestPlaceOrderTransformer.transform(message,flag);
	            }else if(Constants.GUESTCHECKOUT.equalsIgnoreCase(flow)){
	            	body = GuestTransactionsTransformer.transform(message,flag);
	            }else if(Constants.RESERVLOOKUP.equalsIgnoreCase(flow)){
	            	body = ReservationTransformer.transform(message,flag);
	            }else if(Constants.HOTELFOLIO.equalsIgnoreCase(flow)){
	            	body = HotelFolioTransformer.transform(message,flag);
	            }
	            soapConnection.close();
	            arg0.getOut().setBody(body);
	            }
	            if(isNotValidResLookUp){
	            	String body = "{\"ServiceError\":{\"faultstring\":\"An exception has occured.\",\"faultreason\":\"Please provide any of these three: (1). Reservation Confirmation Number (2). Last Name AND Last 4 digits of Credit Card (3). Hotel Loyalty Number\"}}";
		            soapConnection.close();
		            arg0.getOut().setBody(body);
	            }
	        } catch (Exception e) {
	            System.err.println("Error occurred while sending SOAP Request to Server");
	            e.printStackTrace();
	            String mesg = "DynamicRouteProcessor: process: "+e.toString();
	            if(1 == new SendEmail().sendEmail(emailSource.getHOST(), emailSource.getFROM_EMAIL(), emailSource.getADMIN_EMAIL(), emailSource.getPASS(), emailSource.getPORT(), null, "Exception occured at DynamicRouteProcessor.", mesg)){
					arg0.getOut().setBody("<Message><Failure>An exception has occured. An email is sent to Admin.</Failure></Message>");
				}else{
					arg0.getOut().setBody("<Message><Failure>An exception has occured. Email sending to Admin failed too.</Failure></Message>");
				}
	        }
	        
			//*******************************************
		}else{
			//Email Admin
			String mesg = "Can not connect to the URL at: "+url;
			if(1 == new SendEmail().sendEmail(emailSource.getHOST(), emailSource.getFROM_EMAIL(), emailSource.getADMIN_EMAIL(), emailSource.getPASS(), emailSource.getPORT(), null, "Provided URL not found.", mesg)){
				arg0.getOut().setBody("<Message><Failure>The PMS point couldn't connect at the URL. An email is sent to Admin.</Failure></Message>");
			}else{
				arg0.getOut().setBody("<Message><Failure>The PMS point couldn't connect at the URL. Email sending to Admin failed too.</Failure></Message>");
			}
		}
	}
	
	private boolean isConnectable(String ur){
		boolean flag = false;
		int i = 0;
		while(i<5){
			try {
					   URL u = new URL ( ur );
					   HttpURLConnection huc = ( HttpURLConnection )  u.openConnection ();
					   huc.setRequestMethod ("GET");
						   System.out.println("Try No: "+(i+1));
						   huc.connect () ;
						   flag = true;
					   int code = huc.getResponseCode ( ) ;
					   System.out.println(code);
					   break;
				} catch (MalformedURLException e) {
					//e.printStackTrace();
				} catch (ProtocolException e) {
					//e.printStackTrace();
				} catch (IOException e) {
					//e.printStackTrace();
				}
			i++;
		}
		if(flag){
			System.out.println("Done");
		}else{
			System.out.println("Couldn't connect.");
		}
		return flag;
	}
	
	private static SOAPMessage createSOAPRequestForGuestBillInfo(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://webservice.integration.ige/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String lastName=XMLElementExtractor.extractXmlElementValue(value, "lastName");
        //soapBodyElem1.addTextNode(lastName);
        //SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("memberShipNumber", "bil");
        String email=XMLElementExtractor.extractXmlElementValue(value, "email");
        //soapBodyElem2.addTextNode(membership);
        //SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roomNumber", "bil");
        String room=XMLElementExtractor.extractXmlElementValue(value, "roomNumber");
        //soapBodyElem3.addTextNode(room);
        
        System.out.println("****************");
        System.out.println(lastName+","+email+","+room);
        System.out.println("****************");
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("getBillInfo", "web");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("lastName");
        soapBodyElem1.addTextNode(lastName);
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("email");
        soapBodyElem2.addTextNode(email);
        SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roomNumber");
        soapBodyElem3.addTextNode(room);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "getBillInfo");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	
	private static SOAPMessage createSOAPRequestForReservationLookup(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://webservice.integration.ige/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String confirmationNumber=XMLElementExtractor.extractXmlElementValue(value, "confirmationNumber");
        String lastName = XMLElementExtractor.extractXmlElementValue(value, "lastName");
        String creditCard = XMLElementExtractor.extractXmlElementValue(value, "maskedCardNumber");
        String loyaltyNum = XMLElementExtractor.extractXmlElementValue(value, "loyaltyCardNumber");
        boolean flag = false;
        if(null != confirmationNumber && !"".equalsIgnoreCase(confirmationNumber.trim())){
        	flag = true;
        }else if((null != lastName || null != creditCard) && (!"".equalsIgnoreCase(lastName.trim()) || !"".equalsIgnoreCase(creditCard.trim()))){
        	flag = true;
        }else if(null != loyaltyNum && !"".equalsIgnoreCase(loyaltyNum.trim())){
        	flag = true;
        }
        
        if(!flag){
        	return null;
        }
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("reservationLookup", "web");
        SOAPElement soapBodyEleme = soapBodyElem.addChildElement("reservationDetails");
        SOAPElement soapBodyElem1 = null;
        if(null != confirmationNumber && !"".equalsIgnoreCase(confirmationNumber.trim())){
	        soapBodyElem1 = soapBodyEleme.addChildElement("confirmationNumber");
	        soapBodyElem1.addTextNode(confirmationNumber);
        }
        if(null != lastName && !"".equalsIgnoreCase(lastName.trim()) && null != creditCard && !"".equalsIgnoreCase(creditCard.trim())){
	        SOAPElement soapBodyElem2 = soapBodyEleme.addChildElement("lastName");
	        soapBodyElem2.addTextNode(lastName);
	        SOAPElement soapBodyElem3 = soapBodyEleme.addChildElement("maskedCardNumber");
	        soapBodyElem3.addTextNode(creditCard);
        }
        if(null != loyaltyNum && !"".equalsIgnoreCase(loyaltyNum.trim())){
	        SOAPElement soapBodyElem4 = soapBodyEleme.addChildElement("loyaltyCardNumber");
	        soapBodyElem4.addTextNode(loyaltyNum);
        }

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "reservationDetails");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	private static SOAPMessage createSOAPRequestForHotelFolio(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://webservice.integration.ige/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String terminalId=XMLElementExtractor.extractXmlElementValue(value, "terminalId");
        String reservationNumber = XMLElementExtractor.extractXmlElementValue(value, "reservationNumber");
        String folioType = XMLElementExtractor.extractXmlElementValue(value, "folioType");
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("HotelFolio", "web");
        SOAPElement soapBodyElem1 = null;
        if(null != terminalId && !"".equalsIgnoreCase(terminalId.trim())){
	        soapBodyElem1 = soapBodyElem.addChildElement("terminalId");
	        soapBodyElem1.addTextNode(terminalId);
        }
        if(null != reservationNumber && !"".equalsIgnoreCase(reservationNumber.trim())){
	        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("reservationNumber");
	        soapBodyElem2.addTextNode(reservationNumber);
        }
        if(null != folioType && !"".equalsIgnoreCase(folioType.trim())){
	        SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("folioType");
	        soapBodyElem4.addTextNode(folioType);
        }

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "HotelFolio");
        
        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	
	private static SOAPMessage createSOAPRequestForGuestCheckOut(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://webservice.integration.ige/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String lastName=XMLElementExtractor.extractXmlElementValue(value, "lastName");
        //soapBodyElem1.addTextNode(lastName);
        //SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("memberShipNumber", "bil");
        String email=XMLElementExtractor.extractXmlElementValue(value, "email");
        //soapBodyElem2.addTextNode(membership);
        //SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roomNumber", "bil");
        String cardNum=XMLElementExtractor.extractXmlElementValue(value, "creditCardNumber");
        String roomNum=XMLElementExtractor.extractXmlElementValue(value, "roomNumber");
        //soapBodyElem3.addTextNode(room);
        
        System.out.println("****************");
        System.out.println(lastName+","+email+","+cardNum+","+roomNum);
        System.out.println("****************");
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("guestCheckout", "web");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("lastName");
        soapBodyElem1.addTextNode(lastName);
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("email");
        soapBodyElem2.addTextNode(email);
        SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("creditCardNumber");
        soapBodyElem3.addTextNode(cardNum);
        SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("roomNumber");
        soapBodyElem4.addTextNode(roomNum);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "guestCheckout");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	
	private static SOAPMessage createSOAPRequestForGuestCheckIn(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://webservice.integration.ige/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String[] guestInfo = {"firstName","lastName","fullAddress","mobileNumber","ratePlan","loyaltyNumber","confirmationNumber","membershipNumber","bonusCode","groupName","email"};
        String[] guestStayInfo = {"roomNumber","floorNumber","arrivalDate","departureDate","folioNumber","totalBill","paymentType","creditAmount","cardType","cardNumber","balanceAmount","roomType","numberOfChildren","numberOfAdult","roomRate","creditCardExpirationDate","rateCode","reservationType"};
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("guestCheckIn", "web");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("guestInfo");
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("guestStayInfo");
        Map<String,String> mapSoapElements = new HashMap<String,String>();
        for(int x=0;x<guestInfo.length;x++){
        	mapSoapElements.put(guestInfo[x],XMLElementExtractor.extractXmlElementValue(value, guestInfo[x]));
        }
        for(int y=0;y<guestStayInfo.length;y++){
        	mapSoapElements.put(guestStayInfo[y],XMLElementExtractor.extractXmlElementValue(value, guestStayInfo[y]));
        }
        
        SOAPElement [] soapElem = new SOAPElement[mapSoapElements.size()] ;
        //Iterate through the map to set SOAPElement and Body
        
        for(int i=0;i<guestInfo.length;i++){
        	String key = guestInfo[i];
        	System.out.println(key);
        	soapElem[i] = soapBodyElem1.addChildElement(key);
        	if(null == mapSoapElements.get(key))
              	soapElem[i].addTextNode("");
              else
              	soapElem[i].addTextNode(mapSoapElements.get(key));
        }
        for(int j=0;j<guestStayInfo.length;j++){
        	String key = guestStayInfo[j];
        	System.out.println(key);
        	soapElem[j] = soapBodyElem2.addChildElement(key);
        	if(null == mapSoapElements.get(key))
              	soapElem[j].addTextNode("");
              else
              	soapElem[j].addTextNode(mapSoapElements.get(key));
        }

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "guestCheckIn");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	
	private static SOAPMessage createSOAPRequestForPlaceOrder(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        String originalVal = value;
        String serverURI = "http://webservice.integration.ige/";
        
        int ind1 = value.indexOf("<guestTransactions>");
        int ind2 = value.indexOf("</guestTransactions>");
        value = value.substring(ind1,ind2);
        System.out.println(value);
        List<String> text = new ArrayList<String>();
        while(-1 != value.indexOf("<item>")){
        	ind1 = value.indexOf("<item>");
        	ind2 = value.indexOf("</item>");
        	String v = value.substring(ind1,ind2+7);
        	System.out.println(v);
        	text.add(v);
        	value = value.substring(ind2+7);
        }

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String[] someInfo = {"lastName","email","roomNumber"};
        String[] guestTransactions = {"transactionDate","description","referenceNumber","transactionId","charges"};
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("placeOrder", "web");
        Map<String,String> mapSoapElements = new HashMap<String,String>();
        for(int x=0;x<someInfo.length;x++){
        	mapSoapElements.put(someInfo[x],XMLElementExtractor.extractXmlElementValue(originalVal, someInfo[x]));
        }
        int index = 0;
        while(text.size() >  index){
	        for(int j=0;j<guestTransactions.length;j++){
	        	String val = text.get(index);
	        	System.out.println("HERE: "+val);
	        	mapSoapElements.put(val,XMLElementExtractor.extractXmlElementValue(val, guestTransactions[j]));
	        }
	        index++;
        }
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("guestTransactions");
        SOAPElement soapBodyElem2 = null;
        
        SOAPElement [] soapElem = new SOAPElement[mapSoapElements.size()] ;
        //Iterate through the map to set SOAPElement and Body
        
        for(int i=0;i<someInfo.length;i++){
        	String key = someInfo[i];
        	System.out.println(key);
        	soapElem[i] = soapBodyElem.addChildElement(key);
        	if(null == mapSoapElements.get(key))
              	soapElem[i].addTextNode("");
              else
              	soapElem[i].addTextNode(mapSoapElements.get(key));
        }
        index = 0;
        while(text.size() >  index){
        	soapBodyElem2 = soapBodyElem1.addChildElement("item");
	        for(int j=0;j<guestTransactions.length;j++){
	        	String key = guestTransactions[j];
	        	System.out.println(key);
	        	soapElem[j] = soapBodyElem2.addChildElement(key);
	        	if(null == XMLElementExtractor.extractXmlElementValue(text.get(index), guestTransactions[j]))
	              	soapElem[j].addTextNode("");
	              else
	              	soapElem[j].addTextNode(XMLElementExtractor.extractXmlElementValue(text.get(index), guestTransactions[j]));
	        }
	        index++;
        }

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "guestCheckIn");

        soapMessage.saveChanges();

        // Print the request message 
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }

    /**
     * Method used to print the SOAP Response
     */
    private static String printSOAPResponse(SOAPMessage soapResponse) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        Source sourceContent = soapResponse.getSOAPPart().getContent();
        System.out.print("\nResponse SOAP Message = ");
        StreamResult result = new StreamResult(System.out);
        transformer.transform(sourceContent, result);
        
        System.out.println();
        StringWriter writer = new StringWriter();
        transformer.transform(sourceContent, new StreamResult(writer));
        String output = writer.toString();
        int ind = output.indexOf("<S");
        System.out.println("NOW IS: "+output.substring(ind));
        return output;
    }

}
