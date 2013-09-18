package ige.integration.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
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

    public int sendEmail(String hostName, final String fromEmail, String toEmail, final String password, String port, String file, String subject, String msg)
    {
        // Get system properties
        Properties props = System.getProperties();

        // Setup mail server
        props.put("mail.smtp.host", hostName);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.user", fromEmail);
        props.put("mail.smtp.password", password);
        props.put("mail.debug", "true");
        System.out.println(hostName);
        System.out.println(port);
        System.out.println(fromEmail);
        System.out.println(password);
        System.out.println(toEmail);
        System.out.println(file);
        // Get session
        /*Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });*/
        Session session = Session.getInstance(props, new GMailAuthenticator(fromEmail, password));

        System.out.println("REACHED-1");
        session.setDebug(true);

        // Define message
        MimeMessage message = null;
        MimeBodyPart messageBodyPart1 = null;
        try{
        message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
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
        String body = "<html><body>Hello, please find the attached Report...<br/><br/><br/>Regards...<br/>Acenonyx</body></html>";
        System.out.println("REACHED-4");
        try{
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setHeader("Content-Type", "text/plain; charset=\"utf-8\"");
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
    	if(1 == new SendEmail().sendEmail("smtp.gmail.com", fromEmail, toEmail, password, port, file, subject, message)){
    		System.out.println("Email Sent!!!");
    	}else{
    		System.out.println("Failure to Send");
    	}
    }
}