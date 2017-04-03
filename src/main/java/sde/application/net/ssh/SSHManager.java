package sde.application.net.ssh;

import sde.application.error.Error;
import sde.application.node.design.DrawableNode;
import sde.application.node.implementations.ConsoleNode;
import sde.application.node.implementations.LinuxNode;
import sde.application.utils.SDERunnable;
import sde.application.utils.SDEThread;
import sde.application.utils.managers.SSHConnectionManager;
import com.jcraft.jsch.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SSHManager {
    private JSch jschSSHChannel;
    private String strUserName;
    private String strConnectionIP;
    private int intConnectionPort;
    private String strPassword;
    private Session sesConnection;
    private int intTimeOut;
    private PipedInputStream pip;
    private PipedOutputStream pop;
    private PrintStream print;
    private PipedInputStream sink;
    private DrawableNode drawableNode;
    private List<SSHCommand> SSHCommandList = new ArrayList<>();
    private Boolean processingCommand = false;

    private static Logger log = Logger.getLogger(SSHManager.class);

    private void doCommonConstructorActions(String userName, String password, String connectionIP, String knownHostsFileName) {
        jschSSHChannel = new JSch();

        try {
            jschSSHChannel.setKnownHosts(knownHostsFileName);
        } catch (JSchException ex) {
            Error.OPEN_SSH_CONNECTION.record().create(ex);
        }

        strUserName = userName;
        strPassword = password;
        strConnectionIP = connectionIP;
    }

    public SSHManager(String userName, String password, String connectionIP,
                      String knownHostsFileName, int connectionPort) {
        doCommonConstructorActions(userName, password, connectionIP,
                knownHostsFileName);
        intConnectionPort = connectionPort;
        intTimeOut = 60000;
        SSHConnectionManager.getInstance().addConnection(this);
    }

    public void setDrawableNode(DrawableNode drawableNode) {
        this.drawableNode = drawableNode;
    }

    public String connect() {
        String errorMessage = null;

        try {
            sesConnection = jschSSHChannel.getSession(strUserName, strConnectionIP, intConnectionPort);
            sesConnection.setPassword(strPassword);
            // UNCOMMENT THIS FOR TESTING PURPOSES, BUT DO NOT USE IN PRODUCTION
            // sesConnection.setConfig("StrictHostKeyChecking", "no");
            sesConnection.connect(intTimeOut);
        } catch (JSchException ex) {
            Error.OPEN_SSH_CONNECTION.record().create(ex);
        }

        return errorMessage;
    }

    private String sendCommand(String command) {
        StringBuilder outputBuffer = new StringBuilder();

        try {
            Channel channel = sesConnection.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.connect();
            InputStream commandOutput = channel.getInputStream();
            int readByte = commandOutput.read();

            while (readByte != 0xffffffff) {
                outputBuffer.append((char) readByte);
                readByte = commandOutput.read();
            }

            channel.disconnect();
        } catch (IOException | JSchException ex) {
            Error.RUN_SSH_COMMAND.record().create(ex);
            return null;
        }

        return outputBuffer.toString();
    }

    public void close() {
        sesConnection.disconnect();
        try {
            if (sink != null) {
                sink.close();
            }
        } catch (IOException ex) {
            Error.CLOSE_SSH_CONNECTION.record().create(ex);
        }
        try {
            if (pip != null) {
                pip.close();
            }
        } catch (IOException ex) {
            Error.CLOSE_SSH_CONNECTION.record().create(ex);
        }
        try {
            if (pop != null) {
                pop.close();
            }
        } catch (IOException ex) {
            Error.CLOSE_SSH_CONNECTION.record().create(ex);
        }

        if (print != null) {
            print.close();
        }
    }

    static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }

    public void scpFrom(String remoteFile, String localFile) {
        FileOutputStream fos = null;
        try {
            String prefix = null;
            if (new File(localFile).isDirectory()) {
                prefix = localFile + File.separator;
            }

            // exec 'scp -f rfile' remotely
            String command = "scp -f " + remoteFile;
            Channel channel = sesConnection.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            while (true) {
                int c = checkAck(in);
                if (c != 'C') {
                    break;
                }

                // read '0644 '
                int response = in.read(buf, 0, 5);

                long fileSize = 0L;
                while (true) {
                    if (in.read(buf, 0, 1) < 0) {
                        // error
                        break;
                    }
                    if (buf[0] == ' ') break;
                    fileSize = fileSize * 10L + (long) (buf[0] - '0');
                }

                String file;
                for (int i = 0; ; i++) {
                    in.read(buf, i, 1);
                    if (buf[i] == (byte) 0x0a) {
                        file = new String(buf, 0, i);
                        break;
                    }
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                // read a content of lfile
                fos = new FileOutputStream(prefix == null ? localFile : prefix + file);
                int foo;
                while (true) {
                    if (buf.length < fileSize) foo = buf.length;
                    else foo = (int) fileSize;
                    foo = in.read(buf, 0, foo);
                    if (foo < 0) {
                        // error
                        break;
                    }
                    fos.write(buf, 0, foo);
                    fileSize -= foo;
                    if (fileSize == 0L) break;
                }
                fos.close();
                fos = null;

                if (checkAck(in) != 0) {
                    //System.exit(0);
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();
            }
        } catch (Exception ex) {
            Error.SCP_COPY.record().create(ex);
            try {
                if (fos != null) fos.close();
            } catch (Exception ex2) {
                Error.CLOSE_FILE_STREAM.record().create(ex2);
            }
        }
    }

    public Channel openExecChannel() {
        try {
            return sesConnection.openChannel("exec");
        } catch (JSchException ex) {
            Error.OPEN_SSH_CONNECTION.record().create(ex);
        }
        return null;
    }

    public void scpTo(String remoteFile, String localFile) {
        FileInputStream fis = null;
        try {
            // exec 'scp -t rfile' remotely
            String fileDirectory = remoteFile.substring(0, remoteFile.lastIndexOf("/"));
            String scpCommand = "scp " + ("-p") + " -t \"" + remoteFile + "\"";
            String command = "if test -d \"" + fileDirectory + "\"/; then " + scpCommand + "; else mkdir -p \"" + fileDirectory + "\" && " + scpCommand + "; fi";
            Channel channel = sesConnection.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                return;
            }

            File _lfile = new File(localFile);

            command = "T " + (_lfile.lastModified() / 1000) + " 0";
            // The access time should be sent here,
            // but it is not accessible with JavaAPI ;-<
            command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
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
            if (checkAck(in) != 0) {
                return;
            }

            // send a content of lfile
            fis = new FileInputStream(localFile);
            byte[] buf = new byte[1024];
            long sendBytesCount = 0;
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); //out.flush();
                sendBytesCount += buf.length;

                log.info(LinuxNode.humanReadableByteCount(sendBytesCount, false));
            }
            fis.close();
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                return;
            }
            out.close();

            channel.disconnect();
        } catch (Exception ex) {
            Error.SCP_COPY.record().create(ex);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex2) {
                    Error.CLOSE_FILE_STREAM.record().create(ex2);
                }
            }
        }
    }

    public void runSSHCommand(SSHCommand command) {
        SSHCommandList.add(command);

        runNextCommand();
    }

    private void runNextCommand() {
        if (SSHCommandList.size() > 0) {
            if (!processingCommand) {
                processingCommand = true;
                sendShellCommand(SSHCommandList.get(0).getCommand());
            }
        }
    }

    public void createShellChannel() {
        try {
            Channel channel = sesConnection.openChannel("shell");

            pip = new PipedInputStream(40);
            pop = new PipedOutputStream(pip);
            print = new PrintStream(pop);

            PipedOutputStream source = new PipedOutputStream();
            sink = new PipedInputStream();
            source.connect(sink);

            new SDEThread(new SDERunnable() {
                public void threadRun() {
                    int bufferSize = 131072;
                    byte[] data = new byte[bufferSize];
                    try {
                        int i = sink.read(data, 0, bufferSize);
                        String previousString = "";
                        while (true) {
                            if (i < 0) break;

                            String responseString = new String(data, 0, i);

                            if (SSHCommandList.size() > 0) {
                                if ("".equals(SSHCommandList.get(0).getReturnString()) || responseString.contains(SSHCommandList.get(0).getReturnString())) {
                                    if (!responseString.equals(previousString)) {
                                        log.info("responseString - " + responseString + " - Completed command " + SSHCommandList.get(0).getCommand());
                                        SSHCommandList.remove(SSHCommandList.get(0));
                                        processingCommand = false;
                                        runNextCommand();
                                    }
                                }
                            }
                            previousString = responseString;
                            if (drawableNode != null) {
                                if (drawableNode instanceof ConsoleNode) {
                                    ((ConsoleNode) drawableNode).writeToConsole(responseString);
                                } else if (drawableNode instanceof LinuxNode) {
                                    ((LinuxNode) drawableNode).writeToConsole(responseString);
                                }
                            }
                            i = sink.read(data, 0, bufferSize);
                        }
                    } catch (IOException ex) {
                        Error.OPEN_SSH_CONNECTION.record().create(ex);
                    }
                }
            }, "SSH Shell", null, true);

            channel.setInputStream(pip);
            channel.setOutputStream(source);
            channel.connect(3 * 1000);
        } catch (JSchException | IOException ex) {
            Error.OPEN_SSH_CONNECTION.record().create(ex);
        }
    }

    private void sendShellCommand(String command) {
        log.info("running command " + command);
        print.print("  " + "\n");
        print.print(command + "\n");
        print.print("  " + "\n");
    }

    public Boolean isConnected() {
        return sesConnection != null && sesConnection.isConnected();
    }
}