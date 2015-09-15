package application.utils;

import org.apache.log4j.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailMessage {
    private String from = "";
    private String subject = "";
    private String content = "";
    private String host = "";
    private String protocol = "";
    private String username = "";
    private String password = "";
    private String port = "";
    private List<String> recipients = new ArrayList<>();
    private List<String> attachFileNames = new ArrayList<>();

    private static Logger log = Logger.getLogger(EmailMessage.class);

    public EmailMessage() {

    }

    public EmailMessage password(String password) {
        this.password = password;
        return this;
    }

    public EmailMessage username(String username) {
        this.username = username;
        return this;
    }

    public EmailMessage attach(String attachFileName) {
        attachFileNames.add(attachFileName);
        return this;
    }

    public EmailMessage protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public EmailMessage from(String from) {
        this.from = from;
        return this;
    }

    public EmailMessage to(String to) {
        recipients.add(to);
        return this;
    }

    public EmailMessage subject(String subject) {
        this.subject = subject;
        return this;
    }

    public EmailMessage content(String content) {
        this.content = content;
        return this;
    }

    public EmailMessage host(String host) {
        this.host = host;
        return this;
    }

    public EmailMessage port(String port) {
        this.port = port;
        return this;
    }

    public void send() {
        new SDEThread(new SendEmailMessage(), "Thread for sending email");
    }

    private class SendEmailMessage extends SDERunnable {
        public void threadRun() {
            Properties props = System.getProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port); // 587

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            try {
                // Create a default MimeMessage object.
                MimeMessage message = new MimeMessage(session);

                // Set From: header field of the header.
                message.setFrom(new InternetAddress(from));

                // Set To: header field of the header.
                for (String recipient : recipients) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                }

                // Set Subject: header field
                message.setSubject(subject);

                // Now set the actual message and attach any files
                if (attachFileNames.size() > 0) {
                    Multipart multipart = new MimeMultipart();
                    BodyPart messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setText(content);

                    for (String fileName : attachFileNames) {
                        DataSource source = new FileDataSource(fileName);
                        messageBodyPart.setDataHandler(new DataHandler(source));
                        messageBodyPart.setFileName(fileName);
                        multipart.addBodyPart(messageBodyPart);
                    }

                    message.setContent(multipart);
                } else {
                    message.setText(content);
                }

                // Send message
                Transport.send(message);

                // Create log to show emails were sent
                StringBuilder sb = new StringBuilder();
                for (String s : recipients) {
                    sb.append(s);
                    sb.append(" ");
                }

                log.info("Email sent out successfully to " + sb.toString());
            } catch (MessagingException mex) {
                log.error("Error sending email", mex);
            }
        }
    }
}
