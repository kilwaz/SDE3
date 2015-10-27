package application.net.ssh;

import application.error.Error;
import application.node.implementations.CopyNode;
import application.node.implementations.LinuxNode;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import org.apache.log4j.Logger;

import java.io.*;

public class SCPProgress {
    private SSHManager sshManager;
    private String remoteFile;
    private String localFile;
    private CopyNode copyNode;

    private static Logger log = Logger.getLogger(SCPProgress.class);

    public SCPProgress(SSHManager sshManager, String remoteFile, String localFile, CopyNode copyNode) {
        this.sshManager = sshManager;
        this.remoteFile = remoteFile;
        this.localFile = localFile;
        this.copyNode = copyNode;
    }

    public void startCopy() {
        FileInputStream fis = null;
        try {
            // exec 'scp -t rfile' remotely
            String fileDirectory = remoteFile.substring(0, remoteFile.lastIndexOf("/"));
            String scpCommand = "scp " + ("-p") + " -t \"" + remoteFile + "\"";
            String command = "if test -d \"" + fileDirectory + "\"/; then " + scpCommand + "; else mkdir -p \"" + fileDirectory + "\" && " + scpCommand + "; fi";

            Channel channel = sshManager.openExecChannel();
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (SSHManager.checkAck(in) != 0) {
                return;
            }

            File _lfile = new File(localFile);

            command = "T " + (_lfile.lastModified() / 1000) + " 0";
            // The access time should be sent here,
            // but it is not accessible with JavaAPI ;-<
            command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
            out.write(command.getBytes());
            out.flush();
            if (SSHManager.checkAck(in) != 0) {
                return;
            }

            // send "C0644 filesize filename", where filename should not include '/'
            long fileSize = _lfile.length();
            command = "C0644 " + fileSize + " ";
            if (localFile.lastIndexOf('/') > 0) {
                command += localFile.substring(localFile.lastIndexOf('/') + 1);
            } else {
                command += localFile;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (SSHManager.checkAck(in) != 0) {
                return;
            }

            // send a content of lfile
            fis = new FileInputStream(localFile);
            byte[] buf = new byte[1024];
            long totalBytesSent = 0;
            long totalBytesToSend = new File(localFile).length();
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); //out.flush();
                totalBytesSent += buf.length;

                Double totalBytesSentDouble = (double) totalBytesSent;
                Double totalBytesToSendDouble = (double) totalBytesToSend;
                copyNode.updateProgressBar((totalBytesSentDouble / totalBytesToSendDouble));
                copyNode.updateProgressBarLabel("Copying " + localFile + " (" + LinuxNode.humanReadableByteCount(totalBytesSent, false) + " / " + LinuxNode.humanReadableByteCount(totalBytesToSend, false) + ")");
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (SSHManager.checkAck(in) != 0) {
                return;
            }
            out.close();

            channel.disconnect();

            copyNode.updateProgressBar(1.0);
            copyNode.updateProgressBarLabel("Completed " + localFile);
        } catch (Exception ex) {
            Error.SCP_COPY.record().create(ex);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Error.CLOSE_FILE_STREAM.record().create(ex);
                }
            }
        }
    }
}
