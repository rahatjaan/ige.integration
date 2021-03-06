package ige.integration.processes;

import ige.integration.constants.Constants;
import ige.integration.constants.EmailSource;
import ige.integration.transformer.BillDetailsTransformer;
import ige.integration.transformer.GetListRoomsTransformer;
import ige.integration.transformer.GuestCheckInTransformer;
import ige.integration.transformer.GuestPlaceOrderTransformer;
import ige.integration.transformer.GuestSignatureTransformer;
import ige.integration.transformer.GuestStayInfoTransformer;
import ige.integration.transformer.GuestTransactionsTransformer;
import ige.integration.transformer.HotelFolioTransformer;
import ige.integration.transformer.PaymentTransformer;
import ige.integration.transformer.ReportProblemTransformer;
import ige.integration.transformer.ReservationTransformer;
import ige.integration.transformer.UpdateGuestStayInfoTransformer;
import ige.integration.transformer.UserPictureTransformer;
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
		String url = arg0.getIn().getHeader("OutboundUrl").toString();
		String flow = arg0.getIn().getHeader("flow").toString();
		System.out.println("URL IS: "+url+" and flow:"+flow);
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
	            
	            if(Constants.GUESTBILLINFO.equalsIgnoreCase(flow) || Constants.SEND_BILL_INFO_EMAIL.equalsIgnoreCase(flow)){
	            	soapResponse = soapConnection.call(createSOAPRequestForGuestBillInfo(req), url);
	            }
	            else if(Constants.SENDEMAIL.equalsIgnoreCase(flow)){
	            	soapResponse = null;
	            }
	            else if(Constants.IGE_GUESTBILLINFO.equalsIgnoreCase(flow)){
	            	soapResponse = soapConnection.call(createSOAPRequestForGuestBillInfo(req), url);
	            }
	            else if(Constants.GUESTCHECKIN.equalsIgnoreCase(flow)){
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
	            }else if(Constants.PAYMENTCARDPROCESSING.equalsIgnoreCase(flow)){
//	            	soapResponse = soapConnection.call(createSOAPRequestForPaymentCardProcessing(req), url);
	            	System.out.println("Payment Processing DONE");
	            }else if(Constants.REPORTPROBLEM.equalsIgnoreCase(flow)){
	            	soapResponse = soapConnection.call(createSOAPRequestForReportProblem(req), url);
	            }else if(Constants.GETGUESTSTAYINFO.equalsIgnoreCase(flow)){
	            	soapResponse = soapConnection.call(createSOAPRequestForGetGuestStayInfo(req), url);
	            }else if(Constants.GETLISTROOMS.equalsIgnoreCase(flow)){
	            	soapResponse = soapConnection.call(createSOAPRequestForGetListRooms(req), url);
	            }else if(Constants.GUESTSIGNATURE.equalsIgnoreCase(flow)){
	            	soapResponse = soapConnection.call(createSOAPRequestForGuestSignature(req), url);
	            }else if(Constants.USERPICTURE.equalsIgnoreCase(flow)){
	            	soapResponse = soapConnection.call(createSOAPRequestForUserPicture(req), url);
	            }else if(Constants.UPDATEGUESTSTAYINFO.equalsIgnoreCase(flow)){
	            	soapResponse = soapConnection.call(createSOAPRequestForUpdateGuestStayInfo(req), url);
	            }
	            if(!Constants.GUESTCHECKIN.equalsIgnoreCase(flow) && !isNotValidResLookUp){
	            // Process the SOAP Response
	            String message = printSOAPResponse(soapResponse); 
	            boolean flag = false;
	            if(message!=null || !message.isEmpty()){
		            message = message.replaceAll("&lt;","<");
		            message = message.replaceAll("&gt;",">");
		            System.out.println("BEFORE TRANSFORMATION: "+message);
		           
		            if(message.contains("<faultcode>")){
		            	flag = true;
		            }
	            }
	            String body = "";
	            if(Constants.GUESTBILLINFO.equalsIgnoreCase(flow) || Constants.SEND_BILL_INFO_EMAIL.equalsIgnoreCase(flow)){
	            	boolean sendEmail = false;
	            	if(Constants.SEND_BILL_INFO_EMAIL.equalsIgnoreCase(flow)){
	            		sendEmail = true;
	            	}
//	            	message= message+req;
	            	body = BillDetailsTransformer.transform(message,flag,sendEmail,emailSource,req);
	            	
	            }
	            else if(Constants.SENDEMAIL.equalsIgnoreCase(flow)){
	            	System.out.println("request body:"+req);
//	            	String from = XMLElementExtractor.extractXmlElementValue(req, "from");
	            	String to = XMLElementExtractor.extractXmlElementValue(req, "to");
	            	String subject = XMLElementExtractor.extractXmlElementValue(req, "subject");
	            	String content = XMLElementExtractor.extractXmlElementValue(req, "content");
	            	new SendEmail().sendEmail(emailSource.getHOST(),
	            			emailSource.getFROM_EMAIL(), to,
	            			emailSource.getPASS(), emailSource.getPORT(), null,
	            			subject, content,emailSource.getFROM_NAME());
	            	body = "<Message>Email is sent to "+to+"</Message>";
	            			
	            }
	            else if(Constants.IGE_GUESTBILLINFO.equalsIgnoreCase(flow)){
	            	body = BillDetailsTransformer.transformIGEBill(message,flag);
	            }
	            else if(Constants.GUESTCHECKIN.equalsIgnoreCase(flow)){
	            	body = GuestCheckInTransformer.transform(message,flag);
	            }else if(Constants.GUESTPLACEORDER.equalsIgnoreCase(flow)){
	            	body = GuestPlaceOrderTransformer.transform(message,flag);
	            }else if(Constants.GUESTCHECKOUT.equalsIgnoreCase(flow)){
	            	body = GuestTransactionsTransformer.transform(message,flag);
	            }else if(Constants.RESERVLOOKUP.equalsIgnoreCase(flow)){
	            	body = ReservationTransformer.transform(message,flag);
	            }else if(Constants.HOTELFOLIO.equalsIgnoreCase(flow)){
	            	body = HotelFolioTransformer.transform(message,flag);
	            }else if(Constants.PAYMENTCARDPROCESSING.equalsIgnoreCase(flow)){
	            	String authStr ="AuthorisationSuccessful";
	            	if(req.contains("CardOnFile"))
	            		authStr = "PaymentSuccessful";
					//	            	<errorMessage></errorMessage>
	            	body = "<PaymentCardProcessingRS><processType>authorization</processType><transactionStatus>"+authStr +"</transactionStatus></PaymentCardProcessingRS>";
	            }else if(Constants.REPORTPROBLEM.equalsIgnoreCase(flow)){
	            	body = ReportProblemTransformer.transform(message,flag);
	            }else if(Constants.GETGUESTSTAYINFO.equalsIgnoreCase(flow)){
	            	body = GuestStayInfoTransformer.transform(message,flag);
	            }else if(Constants.GETLISTROOMS.equalsIgnoreCase(flow)){
	            	body = GetListRoomsTransformer.transform(message,flag);
	            }else if(Constants.GUESTSIGNATURE.equalsIgnoreCase(flow)){
	            	body = GuestSignatureTransformer.transform(message,flag);
	            }else if(Constants.USERPICTURE.equalsIgnoreCase(flow)){
	            	body = UserPictureTransformer.transform(message,flag);
	            }else if(Constants.UPDATEGUESTSTAYINFO.equalsIgnoreCase(flow)){
	            	body = UpdateGuestStayInfoTransformer.transform(message,flag);
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
	            if(1 == new SendEmail().sendEmail(emailSource.getHOST(), emailSource.getFROM_EMAIL(), emailSource.getADMIN_EMAIL(), emailSource.getPASS(), emailSource.getPORT(), null, "Exception occured at DynamicRouteProcessor.", mesg,emailSource.getFROM_NAME())){
					arg0.getOut().setBody("<Message><Failure>An exception has occured. An email is sent to Admin.</Failure></Message>");
				}else{
					arg0.getOut().setBody("<Message><Failure>An exception has occured. Email sending to Admin failed too.</Failure></Message>");
				}
	        }
	        
			//*******************************************
		}else{
			//Email Admin
			String mesg = "Can not connect to the URL at: "+url;
			if(1 == new SendEmail().sendEmail(emailSource.getHOST(), emailSource.getFROM_EMAIL(), emailSource.getADMIN_EMAIL(), emailSource.getPASS(), emailSource.getPORT(), null, "Provided URL not found.", mesg,emailSource.getFROM_NAME())){
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

        String serverURI = "http://webservice.pms.com/";

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
        
        if(email!=null && !email.isEmpty()){
        	SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("email");
        	soapBodyElem2.addTextNode(email);
        }
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
	
	private static SOAPMessage createSOAPRequestForGetGuestStayInfo(String value) throws Exception {
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
        SOAPElement soapBodyElem = soapBody.addChildElement("getGuestStayInfo", "web");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("lastName");
        soapBodyElem1.addTextNode(lastName);
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("email");
        soapBodyElem2.addTextNode(email);
        SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roomNumber");
        soapBodyElem3.addTextNode(room);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "getGuestStayInfo");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	private static SOAPMessage createSOAPRequestForUpdateGuestStayInfo(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://webservice.pms.com/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String confirmationNumber=XMLElementExtractor.extractXmlElementValue(value, "confirmationNumber");
        String departureDate=XMLElementExtractor.extractXmlElementValue(value, "departureDate");
        String arrivalDate=XMLElementExtractor.extractXmlElementValue(value, "arrivalDate");
        //soapBodyElem1.addTextNode(lastName);
        //SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("memberShipNumber", "bil");
        String cardType=XMLElementExtractor.extractXmlElementValue(value, "cardType");
        String cardNumber=XMLElementExtractor.extractXmlElementValue(value, "cardNumber");
        String specialRequests=XMLElementExtractor.extractXmlElementValue(value, "specialRequests");
        String cvvNumber=XMLElementExtractor.extractXmlElementValue(value, "cvvNumber");
        String folioNumber=XMLElementExtractor.extractXmlElementValue(value, "folioNumber");
        String currencyCode=XMLElementExtractor.extractXmlElementValue(value, "currencyCode");
        String cardExpiryDate=XMLElementExtractor.extractXmlElementValue(value, "cardExpiryDate");
        String numberOfChildren=XMLElementExtractor.extractXmlElementValue(value, "numberOfChildren");
        String numberOfAdults=XMLElementExtractor.extractXmlElementValue(value, "numberOfAdults");
        String isCheckedOut=XMLElementExtractor.extractXmlElementValue(value, "isCheckedOut");
        String namePrefix=XMLElementExtractor.extractXmlElementValue(value, "namePrefix");
        String firstName=XMLElementExtractor.extractXmlElementValue(value, "firstName");
        String lastName=XMLElementExtractor.extractXmlElementValue(value, "lastName");
        String roomId=XMLElementExtractor.extractXmlElementValue(value, "roomId");
        
       
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("updateReservation", "web");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("confirmationNumber");
        soapBodyElem1.addTextNode(confirmationNumber);
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("departureDate");
        soapBodyElem2.addTextNode(departureDate);
        SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("arrivalDate");
        soapBodyElem3.addTextNode(arrivalDate);
        SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("reservationDetails");
        if(null != cardType && !"".equalsIgnoreCase(cardType)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("cardType");
            soapBodyElem5.addTextNode(cardType);
        }
        if(null != cardNumber && !"".equalsIgnoreCase(cardNumber)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("cardNumber");
            soapBodyElem5.addTextNode(cardNumber);
        }
        if(null != specialRequests && !"".equalsIgnoreCase(specialRequests)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("specialRequests");
            soapBodyElem5.addTextNode(specialRequests);
        }
        if(null != cvvNumber && !"".equalsIgnoreCase(cvvNumber)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("cvvNumber");
            soapBodyElem5.addTextNode(cvvNumber);
        }
        if(null != folioNumber && !"".equalsIgnoreCase(folioNumber)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("folioNumber");
            soapBodyElem5.addTextNode(folioNumber);
        }
        if(null != currencyCode && !"".equalsIgnoreCase(currencyCode)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("currencyCode");
            soapBodyElem5.addTextNode(currencyCode);
        }
        if(null != cardExpiryDate && !"".equalsIgnoreCase(cardExpiryDate)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("cardExpiryDate");
            soapBodyElem5.addTextNode(cardExpiryDate);
        }
        if(null != numberOfChildren && !"".equalsIgnoreCase(numberOfChildren)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("numberOfChildren");
            soapBodyElem5.addTextNode(numberOfChildren);
        }
        if(null != numberOfAdults && !"".equalsIgnoreCase(numberOfAdults)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("numberOfAdults");
            soapBodyElem5.addTextNode(numberOfAdults);
        }
        if(null != isCheckedOut && !"".equalsIgnoreCase(isCheckedOut)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("isCheckedOut");
            soapBodyElem5.addTextNode(isCheckedOut);
        }
        if(null != namePrefix && !"".equalsIgnoreCase(namePrefix)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("namePrefix");
            soapBodyElem5.addTextNode(namePrefix);
        }
        if(null != firstName && !"".equalsIgnoreCase(firstName)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("firstName");
            soapBodyElem5.addTextNode(firstName);
        }
        if(null != lastName && !"".equalsIgnoreCase(lastName)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("lastName");
            soapBodyElem5.addTextNode(lastName);
        }
        if(null != roomId && !"".equalsIgnoreCase(roomId)){
        	SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("roomId");
            soapBodyElem5.addTextNode(roomId);
        }

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "updateReservation");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	private static SOAPMessage createSOAPRequestForUserPicture(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://webservice.pms.com/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        //soapBodyElem1.addTextNode(lastName);
        //SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("memberShipNumber", "bil");
        String confirmationNumber=XMLElementExtractor.extractXmlElementValue(value, "confirmationNumber");
        //soapBodyElem2.addTextNode(membership);
        //SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roomNumber", "bil");
        String signatureFile=XMLElementExtractor.extractXmlElementValue(value, "userPic");
        //soapBodyElem3.addTextNode(room);
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("userPicture", "web");
        if(null != confirmationNumber){
	        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("confirmationNumber");
	        soapBodyElem1.addTextNode(confirmationNumber);
        }
        if(null != signatureFile){
	        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("userPic");
	        soapBodyElem1.addTextNode(signatureFile);
        }

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "userPicture");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	private static SOAPMessage createSOAPRequestForGuestSignature(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://webservice.integration.ige/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String terminalId=XMLElementExtractor.extractXmlElementValue(value, "terminalId");
        //soapBodyElem1.addTextNode(lastName);
        //SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("memberShipNumber", "bil");
        String confirmationNumber=XMLElementExtractor.extractXmlElementValue(value, "confirmationNumber");
        //soapBodyElem2.addTextNode(membership);
        //SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roomNumber", "bil");
        String signatureFile=XMLElementExtractor.extractXmlElementValue(value, "signatureFile");
        //soapBodyElem3.addTextNode(room);
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("guestSignature", "web");
        if(null != terminalId){
	        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("terminalId");
	        soapBodyElem1.addTextNode(terminalId);
        }
        if(null != confirmationNumber){
	        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("confirmationNumber");
	        soapBodyElem1.addTextNode(confirmationNumber);
        }
        if(null != signatureFile){
	        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("signatureFile");
	        soapBodyElem1.addTextNode(signatureFile);
        }

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "guestSignature");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	private static SOAPMessage createSOAPRequestForGetListRooms(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://webservice.pms.com/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String fromPrice=XMLElementExtractor.extractXmlElementValue(value, "fromPrice");
        //soapBodyElem1.addTextNode(lastName);
        //SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("memberShipNumber", "bil");
        String toPrice=XMLElementExtractor.extractXmlElementValue(value, "toPrice");
        //soapBodyElem2.addTextNode(membership);
        //SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roomNumber", "bil");
        String isComposite=XMLElementExtractor.extractXmlElementValue(value, "isComposite");
        String isSmoking=XMLElementExtractor.extractXmlElementValue(value, "isSmoking");
        //soapBodyElem3.addTextNode(room);
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("listRooms", "web");
        SOAPElement soapBodyEleme = soapBodyElem.addChildElement("roomList");
        if(null != fromPrice){
	        SOAPElement soapBodyElem1 = soapBodyEleme.addChildElement("fromPrice");
	        soapBodyElem1.addTextNode(fromPrice);
        }
        if(null != toPrice){
	        SOAPElement soapBodyElem1 = soapBodyEleme.addChildElement("toPrice");
	        soapBodyElem1.addTextNode(toPrice);
        }
        if(null != isComposite){
	        SOAPElement soapBodyElem1 = soapBodyEleme.addChildElement("isComposite");
	        soapBodyElem1.addTextNode(isComposite);
        }
        if(null != isSmoking){
	        SOAPElement soapBodyElem1 = soapBodyEleme.addChildElement("isSmoking");
	        soapBodyElem1.addTextNode(isSmoking);
        }

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "listRooms");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message = ");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
	
	private static SOAPMessage createSOAPRequestForReportProblem(String value) throws Exception {
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
        String confirmationNumber=XMLElementExtractor.extractXmlElementValue(value, "confirmationNumber");
        //soapBodyElem2.addTextNode(membership);
        //SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roomNumber", "bil");
        String room=XMLElementExtractor.extractXmlElementValue(value, "roomNumber");
        String problemID=XMLElementExtractor.extractXmlElementValue(value, "problemID");
        String problemMessage=XMLElementExtractor.extractXmlElementValue(value, "problemMessage");
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("reportProblem", "web");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("lastName");
        soapBodyElem1.addTextNode(lastName);
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("confirmationNumber");
        soapBodyElem2.addTextNode(confirmationNumber);
        SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roomNumber");
        soapBodyElem3.addTextNode(room);
        SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("problemID");
        soapBodyElem4.addTextNode(problemID);
        SOAPElement soapBodyElem5 = soapBodyElem.addChildElement("problemMessage");
        soapBodyElem5.addTextNode(problemMessage);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "reportProblem");

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

        String serverURI = "http://webservice.pms.com/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String confirmationNumber=XMLElementExtractor.extractXmlElementValue(value, "confirmationNumber");
        String lastName = XMLElementExtractor.extractXmlElementValue(value, "lastName");
        String creditCard = XMLElementExtractor.extractXmlElementValue(value, "maskedCardNumber");
        String loyaltyNum = XMLElementExtractor.extractXmlElementValue(value, "loyaltyCardNumber");
        String roomNumber = XMLElementExtractor.extractXmlElementValue(value, "roomNumber");
        String requestorId = XMLElementExtractor.extractXmlElementValue(value, "requestorId");
        boolean flag = false;
        if(null != confirmationNumber && !"".equalsIgnoreCase(confirmationNumber.trim())){
        	flag = true;
        }else if((null != lastName || null != creditCard) && (!"".equalsIgnoreCase(lastName.trim()) || !"".equalsIgnoreCase(creditCard.trim()))){
        	flag = true;
        }else if((null != lastName || null != roomNumber) && (!"".equalsIgnoreCase(lastName.trim()) || !"".equalsIgnoreCase(roomNumber.trim()))){
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
        if(null != requestorId && !"".equalsIgnoreCase(requestorId.trim())){
	        soapBodyElem1 = soapBodyEleme.addChildElement("requestorId");
	        soapBodyElem1.addTextNode(requestorId);
        }
        if(null != confirmationNumber && !"".equalsIgnoreCase(confirmationNumber.trim())){
	        soapBodyElem1 = soapBodyEleme.addChildElement("confirmationNumber");
	        soapBodyElem1.addTextNode(confirmationNumber);
        }else if(null != lastName && !"".equalsIgnoreCase(lastName.trim()) && null != creditCard && !"".equalsIgnoreCase(creditCard.trim())){
	        SOAPElement soapBodyElem2 = soapBodyEleme.addChildElement("lastName");
	        soapBodyElem2.addTextNode(lastName);
	        SOAPElement soapBodyElem3 = soapBodyEleme.addChildElement("maskedCardNumber");
	        soapBodyElem3.addTextNode(creditCard);
        }else if(null != lastName && !"".equalsIgnoreCase(lastName.trim()) && null != roomNumber && !"".equalsIgnoreCase(roomNumber.trim())){
	        SOAPElement soapBodyElem2 = soapBodyEleme.addChildElement("lastName");
	        soapBodyElem2.addTextNode(lastName);
	        SOAPElement soapBodyElem3 = soapBodyEleme.addChildElement("roomNumber");
	        soapBodyElem3.addTextNode(roomNumber);
        }else if(null != loyaltyNum && !"".equalsIgnoreCase(loyaltyNum.trim())){
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
	
	private static SOAPMessage createSOAPRequestForPaymentCardProcessing(String value) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://webservice.integration.ige/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("web", serverURI);
        String terminalId=XMLElementExtractor.extractXmlElementValue(value, "terminalId");
        String confirmationNumber = XMLElementExtractor.extractXmlElementValue(value, "confirmationNumber");
        String processType = XMLElementExtractor.extractXmlElementValue(value, "processType");
        String paymentType = XMLElementExtractor.extractXmlElementValue(value, "paymentType");
        String cardType = XMLElementExtractor.extractXmlElementValue(value, "cardType");
        String cardHolderName = XMLElementExtractor.extractXmlElementValue(value, "cardHolderName");
        String cardNumber = XMLElementExtractor.extractXmlElementValue(value, "cardNumber");
        String creditCardExpirationDate = XMLElementExtractor.extractXmlElementValue(value, "creditCardExpirationDate");
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("paymentCardProcessing", "web");
        SOAPElement soapBodyElem1 = null;
        if(null != terminalId && !"".equalsIgnoreCase(terminalId.trim())){
	        soapBodyElem1 = soapBodyElem.addChildElement("terminalId");
	        soapBodyElem1.addTextNode(terminalId);
        }
        if(null != confirmationNumber && !"".equalsIgnoreCase(confirmationNumber.trim())){
	        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("confirmationNumber");
	        soapBodyElem2.addTextNode(confirmationNumber);
        }
        if(null != processType && !"".equalsIgnoreCase(processType.trim())){
	        SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("processType");
	        soapBodyElem4.addTextNode(processType);
        }
        if(null != creditCardExpirationDate && !"".equalsIgnoreCase(creditCardExpirationDate.trim())){
	        SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("creditCardExpirationDate");
	        soapBodyElem4.addTextNode(creditCardExpirationDate);
        }
        if(null != paymentType && !"".equalsIgnoreCase(paymentType.trim())){
	        SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("paymentType");
	        soapBodyElem4.addTextNode(paymentType);
        }
        if(null != cardType && !"".equalsIgnoreCase(cardType.trim())){
	        SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("cardType");
	        soapBodyElem4.addTextNode(cardType);
        }
        if(null != cardHolderName && !"".equalsIgnoreCase(cardHolderName.trim())){
	        SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("cardHolderName");
	        soapBodyElem4.addTextNode(cardHolderName);
        }
        if(null != cardNumber && !"".equalsIgnoreCase(cardNumber.trim())){
	        SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("cardNumber");
	        soapBodyElem4.addTextNode(cardNumber);
        }

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "paymentCardProcessing");
        
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
    	if(soapResponse==null)
    		return "";
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
