package application.utils;

import application.node.implementations.EmailNode;

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

    public Email(String emailUrl, String emailUsername, String emailPassword, EmailNode nodeReference) {
        this.nodeReference = nodeReference;
        this.emailPassword = emailPassword;
        this.emailUrl = emailUrl;
        this.emailUsername = emailUsername;

        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect(emailUrl, emailUsername, emailPassword);
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message msg = inbox.getMessage(inbox.getMessageCount());
            System.out.println("Message count " + inbox.getMessageCount());
            inbox.addMessageCountListener(this);
            triggerCheckEmailTimer();

            Address[] in = msg.getFrom();
            for (Address address : in) {
                System.out.println("FROM:" + address.toString());
            }
//            Multipart mp = (Multipart) msg.getContent();
//            BodyPart bp = mp.getBodyPart(0);
            System.out.println("SENT DATE:" + msg.getSentDate());
            System.out.println("SUBJECT:" + msg.getSubject());
            //System.out.println("CONTENT:" + msg.getContent());
        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }

    @Override
    public void messagesAdded(MessageCountEvent messageCountEvent) {
        System.out.println("New Email!");
        Message[] messages = messageCountEvent.getMessages();
        try {
            for (Message message : messages) {
                Address[] in = message.getFrom();
                for (Address address : in) {
                    System.out.println("FROM:" + address.toString());
                }
                System.out.println("SENT DATE:" + message.getSentDate());
                System.out.println("SUBJECT:" + message.getSubject());

                if (message.getContent() instanceof String) {
                    System.out.println("CONTENT:" + message.getContent());
                } else if (message.getContent() instanceof Multipart) {
                    Multipart mp = (Multipart) message.getContent();
                    BodyPart bp = mp.getBodyPart(0);
                    System.out.println("CONTENT:" + bp.getContent());
                }

                nodeReference.newEmailTrigger();
            }
        } catch (Exception mex) {
            mex.printStackTrace();
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
                System.out.println("Checking email count.. " + inbox.getMessageCount());
            } catch (MessagingException e) {
                e.printStackTrace();
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
