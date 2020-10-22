package org.kutsuki.sheets.invoice;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailManager {
    private static final int RETRY = 5;
    private static final String EMAIL_HOME = "email.home";
    private static final String EMAIL_PASSWORD = "email.password";
    private static final String EMAIL_PROPERTIES = "email.properties";
    private static final String EMAIL_SENTINEL = "email.sentinel";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    private static final String PORT = "587";
    private static final String SIGNATURE = "<br/><br/>--<br/>Sentinel";
    private static final String SMTP = "smtp.ionos.com";
    private static final String TEXT_HTML = "text/html";

    private String fromSentinel;
    private String password;
    private String toHome;

    public EmailManager() {
	try {
	    Properties prop = new Properties();
	    prop.load(EmailManager.class.getClassLoader().getResourceAsStream(EMAIL_PROPERTIES));

	    this.fromSentinel = prop.getProperty(EMAIL_SENTINEL);
	    this.password = prop.getProperty(EMAIL_PASSWORD);
	    this.toHome = prop.getProperty(EMAIL_HOME);
	} catch (IOException e) {
	    throw new IllegalArgumentException(e);
	}
    }

    // email Sentinel
    public boolean emailSentinel(String to, String subject, String body) {
	return email(fromSentinel, to, subject, body, null);
    }

    // email Home
    public boolean emailHome(String subject, String body) {
	return email(fromSentinel, toHome, subject, body, null);
    }

    // email
    public boolean email(final String userName, String to, String subject, String body, List<String> attachments) {
	Properties props = new Properties();
	props.put(MAIL_SMTP_AUTH, Boolean.TRUE);
	props.put(MAIL_SMTP_STARTTLS_ENABLE, Boolean.TRUE);
	props.put(MAIL_SMTP_HOST, SMTP);
	props.put(MAIL_SMTP_PORT, PORT);

	Session session = Session.getInstance(props, new Authenticator() {
	    protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(userName, password);
	    }
	});

	Message message = null;
	try {
	    message = new MimeMessage(session);
	    message.setFrom(new InternetAddress(userName));
	    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
	    message.setSubject(subject);

	    MimeBodyPart mbp1 = new MimeBodyPart();
	    StringBuilder sb = new StringBuilder();
	    sb.append(body);

	    if (userName.equals(fromSentinel)) {
		sb.append(SIGNATURE);
	    }

	    mbp1.setContent(sb.toString(), TEXT_HTML);

	    Multipart mp = new MimeMultipart();
	    mp.addBodyPart(mbp1);

	    if (attachments != null) {
		for (String attachment : attachments) {
		    MimeBodyPart mbp = new MimeBodyPart();
		    mbp.attachFile(attachment);
		    mp.addBodyPart(mbp);
		}
	    }

	    message.setContent(mp);
	} catch (MessagingException | IOException e) {
	    e.printStackTrace();
	}

	return sendMessage(message);
    }

    // sendMessage
    private boolean sendMessage(Message message) {
	boolean success = false;

	if (message != null) {
	    int i = 0;
	    while (i < RETRY && !success) {
		try {
		    Transport.send(message);
		    success = true;
		} catch (MessagingException e) {
		    e.printStackTrace();

		    try {
			// sleep 1 second and then retry
			Thread.sleep(1000);
		    } catch (InterruptedException ie) {
			// ignore
		    }
		}

		i++;
	    }
	}

	return success;
    }
}
