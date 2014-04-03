package ige.integration.utils;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendEmail
{

    public int sendEmail(String hostName, final String fromEmail, String toEmail, final String password, String port, String file, String subject, String msg,String fromName)
    {
        // Get system properties
        Properties props = System.getProperties();
        System.out.println("PORT: "+port);
        props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
        // Get session
        /*Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });*/
		Session session = Session.getInstance(props,
				  new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(fromEmail, password);
					}
				  });

        System.out.println("REACHED-1");
        session.setDebug(true);

        // Define message
        MimeMessage message = null;
        MimeBodyPart messageBodyPart1 = null;
        try{
        message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail,fromName));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject(subject);
        System.out.println("REACHED-2");
        if(file!=null && !file.trim().equalsIgnoreCase("")){
	        // Handle attachment 1
	        messageBodyPart1 = new MimeBodyPart();
	        messageBodyPart1.attachFile(file);
        }
        System.out.println("REACHED-3");
        }catch(AddressException e){
        	System.out.println("1");
        	e.printStackTrace();
        	return 0;
        }catch(MessagingException e1){
        	System.out.println("2");
        	e1.printStackTrace();
        	return 0;
        }catch(IOException e2){
        	System.out.println("3");
        	e2.printStackTrace();
        	return 0;
        }
        // Handle text
        String body = msg;
//        String emailHead = "Dear User";
		body ="<div style=\"background-color:rgb(235,235,235);margin:0px;padding:0px;font-size:16px\">"+
"<div style=\"background:#348DB1;padding:20px;\"><img src=\"http://acenonyx.com/sites/all/themes/acenonyx/logo.png\" ></div>"+
"<table border=\"0\" cellpadding=\"20\" cellspacing=\"0\" width=\"100%\">"+
  "<tbody><tr>"+
    "<td>"+
       "<table style=\"max-width:750px;width:90%\" align=\"center\" border=\"0\"><tbody><tr><td>"+  
	"<div style=\"width:100%;margin:40px 0px 0px;background-color:rgb(255,255,255);border-radius:6px;border-style:solid;border-width:1px 1px 3px;border-color:rgb(204,204,204) rgb(204,204,204) rgb(170,170,170);color:rgb(153,153,153);display:block;font-family:Lato,sans-serif;font-size:16px;font-weight:normal;line-height:20px;min-height:250px;padding:10px 15px 20px;background-image:linear-gradient(to bottom,rgb(255,255,255) 98%,rgb(87,191,227) 98%)\">"+
   	"<div style=\"margin:20px 0px 3px;padding-bottom:10px;font-size:26px;color:rgb(87,191,227);font-weight:500;font-family:Lato,Helvetica,sans-serif;border-bottom:1px solid rgb(238,238,238)\">"+
   	subject +
   	"</div>"+

"<div style=\"margin-top:16px;line-height:24px;padding-bottom:10px;font-size:18px;color:rgb(102,102,102);font-weight:300;font-family:Lato,Helvetica,sans-serif\">"+
	msg+
"<div style=\"clear:both\">&nbsp;</div>"+ 	 
	"</div>"+
  "</td></tr></tbody></table>"+
    "</td>"+
  "</tr>"+
  "<tr>"+
  "<p style=\"font-size:9px;\">You are receiving these emails from acenonyx middleware. Learn why we included this. © 2014, Acenonyx 2014 56 Wellington Road East Brunswick, NJ 08816 USA"+
  "<br/>Sales: sales@acenonyx.com<br/>Support: support@acenonyx.com<br/>For specific information: info@acenonyx.com</p></tr>"+ 
"</tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">"+
 "</div></div>";
        System.out.println("REACHED-4");
        try{
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setHeader("Content-Type", "text/html; charset=\"utf-8\"");
        textPart.setContent(body, "text/html; charset=utf-8");

        MimeMultipart multipart = new MimeMultipart("mixed");

        multipart.addBodyPart(textPart);
        if(file!=null && !file.trim().equalsIgnoreCase("")){
        	multipart.addBodyPart(messageBodyPart1);
        }
        System.out.println("REACHED-5");
        message.setContent(multipart);
        // Send message
        //Transport.send(message);
        System.out.println("REACHED-6");
        Transport transport = session.getTransport("smtp");
        System.out.println("HOSTNAME: "+hostName+" from: "+fromEmail+" pass: "+password);
        transport.connect( hostName,fromEmail,password); //host, 25, "myemailhere", "mypasshere");
        System.out.println("REACHED-7");
        message.saveChanges();
        System.out.println("REACHED-8");
        //transport.sendMessage(message,message.getAllRecipients());
        Transport.send(message);
        transport.close();
        System.out.println("REACHED-9");
        }catch(AddressException e){
        	System.out.println("1");
        	e.printStackTrace();
        	return 0;
        }catch(MessagingException e1){
        	System.out.println("2");
        	e1.printStackTrace();
        	return 0;
        }catch(Exception e2){
        	System.out.println("3");
        	e2.printStackTrace();
        	return 0;
        }
        return 1;
    }
    
    public static void main(String[] args){
    	String host = "smtp.gmail.com";
    	String fromEmail = "igeintegration@gmail.com";
    	String toEmail = "quaidian5@yahoo.com";
    	String password = "rahat547";
    	String port = "587";
    	String file = "C:\\Shakeel\\GitRepos\\Reports\\logo.png";
    	String subject = "TESTING";
    	String message = "";
    	if(1 == new SendEmail().sendEmail("smtp.gmail.com", fromEmail, toEmail, password, port, file, subject, message,"Acenonyx")){
    		System.out.println("Email Sent!!!");
    	}else{
    		System.out.println("Failure to Send");
    	}
    }
}