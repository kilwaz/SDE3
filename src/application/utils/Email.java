package application.utils;

import application.node.implementations.EmailNode;
import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class Email implements MessageCountListener {
    private Timer currentEmailCheckTimer;
    private Boolean checkEmail = true;
    private Folder inbox;
    private EmailNode nodeReference;
    private Boolean active = false;

    private String emailUrl = "";
    private String emailUsername = "";
    private String emailPassword = "";

    private static Logger log = Logger.getLogger(Email.class);

    public Email(String emailUrl, String emailUsername, String emailPassword, EmailNode nodeReference) {
        this.nodeReference = nodeReference;
        this.emailPassword = emailPassword;
        this.emailUrl = emailUrl;
        this.emailUsername = emailUsername;
    }

    public void openInbox() {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect(emailUrl, emailUsername, emailPassword);
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message msg = inbox.getMessage(inbox.getMessageCount());
            log.info("Message count " + inbox.getMessageCount());
            inbox.addMessageCountListener(this);
            triggerCheckEmailTimer();

            Address[] in = msg.getFrom();
            for (Address address : in) {
                log.info("FROM:" + address.toString());
            }

            log.info("SENT DATE:" + msg.getSentDate());
            log.info("SUBJECT:" + msg.getSubject());
        } catch (Exception ex) {
            log.error("Error opening inbox",ex);
        }
    }

    @Override
    public void messagesAdded(MessageCountEvent messageCountEvent) {
        log.info("New Email!");
        Message[] messages = messageCountEvent.getMessages();
        try {
            for (Message message : messages) {
                Address[] in = message.getFrom();
                for (Address address : in) {
                    log.info("FROM:" + address.toString());
                }
                log.info("SENT DATE:" + message.getSentDate());
                log.info("SUBJECT:" + message.getSubject());

                if (message.getContent() instanceof String) {
                    log.info("CONTENT:" + message.getContent());
                } else if (message.getContent() instanceof Multipart) {
                    Multipart mp = (Multipart) message.getContent();
                    BodyPart bp = mp.getBodyPart(0);
                    log.info("CONTENT:" + bp.getContent());
                }

                nodeReference.newEmailTrigger();
            }
        } catch (Exception ex) {
            log.error("Error reading email",ex);
        }
    }

    @Override
    public void messagesRemoved(MessageCountEvent messageCountEvent) {

    }

    private void triggerCheckEmailTimer() {
        active = true;
        currentEmailCheckTimer = new Timer();  //At this line a new Thread will be created
        currentEmailCheckTimer.schedule(new ActiveRefreshTimer(), 5000); //delay in milliseconds
    }

    class ActiveRefreshTimer extends TimerTask {
        @Override
        public void run() {
            currentEmailCheckTimer.cancel();

            try {
                // This is to update the inbox to trigger the change listener, we don't actually use the result of this
                log.info("Checking email count.. " + inbox.getMessageCount());
            } catch (MessagingException ex) {
                log.error("Error checking email",ex);
            }

            currentEmailCheckTimer = null;

            if (checkEmail) {
                triggerCheckEmailTimer();
            }
        }
    }

    public void close() {
        currentEmailCheckTimer.cancel();
        active = false;
    }
}
