package application.utils;

import application.error.Error;
import application.node.implementations.EmailNode;
import application.utils.managers.JobManager;
import application.utils.timers.CheckEmailJob;
import org.apache.log4j.Logger;
import org.quartz.*;

import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import java.util.Properties;
import java.util.Timer;

public class Email implements MessageCountListener {
    private static Logger log = Logger.getLogger(Email.class);
    private Timer currentEmailCheckTimer;
    private Boolean checkEmail = true;
    private Folder inbox;
    private EmailNode nodeReference;
    private Boolean active = false;
    private String emailUrl = "";
    private String emailUsername = "";
    private String emailPassword = "";

    private JobKey emailCheckJobKey = null;

    public Email(String emailUrl, String emailUsername, String emailPassword, EmailNode nodeReference) {
        this.nodeReference = nodeReference;
        this.emailPassword = emailPassword;
        this.emailUrl = emailUrl;
        this.emailUsername = emailUsername;
        openInbox();
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
            //Message msg = inbox.getMessage(inbox.getMessageCount());
            //log.info("Message count " + inbox.getMessageCount());
            inbox.addMessageCountListener(this);

            startEmailCheckJob();
//
//            Address[] in = msg.getFrom();
//            for (Address address : in) {
//                log.info("FROM:" + address.toString());
//            }
//
//            log.info("SENT DATE:" + msg.getSentDate());
//            log.info("SUBJECT:" + msg.getSubject());
        } catch (Exception ex) {
            Error.EMAIL_OPEN_INBOX.record().create(ex);
        }
    }

    @Override
    public void messagesAdded(MessageCountEvent messageCountEvent) {
        //log.info("New Email!");
        Message[] messages = messageCountEvent.getMessages();
        try {
            for (Message message : messages) {
                ReceivedEmail receivedEmail = new ReceivedEmail();
                Address[] in = message.getFrom();
                for (Address address : in) {
                    //log.info("FROM:" + address.toString());
                    receivedEmail.setFromAddress(address.toString());
                }
                //log.info("SENT DATE:" + message.getSentDate());
                receivedEmail.setSent(message.getSentDate());
                //log.info("SUBJECT:" + message.getSubject());
                receivedEmail.setSubject(message.getSubject());

                if (message.getContent() instanceof String) {
                    //log.info("CONTENT:" + message.getContent());
                    receivedEmail.setContent(message.getContent().toString());
                } else if (message.getContent() instanceof Multipart) {
                    Multipart mp = (Multipart) message.getContent();
                    BodyPart bp = mp.getBodyPart(0);
                    //log.info("CONTENT:" + bp.getContent());
                    receivedEmail.setContent(bp.getContent().toString());
                }

                nodeReference.newEmailTrigger(receivedEmail);
            }
        } catch (Exception ex) {
            Error.EMAIL_READ.record().create(ex);
        }
    }

    @Override
    public void messagesRemoved(MessageCountEvent messageCountEvent) {

    }

    private void startEmailCheckJob() {
        active = true;

        // Setup job for checking emails
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("email", this);

        JobDetail checkEmailJob = JobBuilder.newJob(CheckEmailJob.class).usingJobData(jobDataMap).build();
        emailCheckJobKey = checkEmailJob.getKey();
        SimpleScheduleBuilder checkEmailSimpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        TriggerBuilder checkEmailTriggerBuilder = TriggerBuilder.newTrigger();
        checkEmailSimpleScheduleBuilder.repeatForever().withIntervalInMilliseconds(5000);
        JobManager.getInstance().scheduleJob(checkEmailJob, checkEmailTriggerBuilder.withSchedule(checkEmailSimpleScheduleBuilder).build());
        checkEmailTriggerBuilder.startNow();
    }

    public void stop() {
        JobManager.getInstance().stopJob(emailCheckJobKey);
        active = false;
    }

    public void touchEmail() {
        try {
            // This is to update the inbox to trigger the change listener, we don't actually use the result of this
            inbox.getMessageCount();
        } catch (MessagingException ex) {
            Error.EMAIL_CHECK.record().create(ex);
        }
    }
}
