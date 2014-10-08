package application.gui;

import com.jcraft.jsch.*;
import main.*;
import main.Frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class SwingConsoleTextArea extends JComponent implements Frame, ActionListener, Runnable {
    private JCTermSwing terminal;
    private String configName;
    private Thread thread = null;
    private main.Frame frame = this;
    private String user = "";
    private String host = "127.0.0.1";
    private String xhost = "127.0.0.1";
    private int xport = 0;
    private boolean xforwarding = false;
    private JSchSession jschsession = null;
    private Proxy proxy = null;
    private int compression = 0;
    private int mode = SHELL;
    private Connection connection = null;
    private Channel channel = null;
    private static int counter = 1;
    private SwingConsoleTextArea textArea = this;

    public SwingConsoleTextArea() {
        terminal = new JCTermSwing();

//        ComponentAdapter l = new ComponentAdapter() {
//            public void componentResized(ComponentEvent e) {
//                Component c = e.getComponent();
//                Container cp = ((JFrame) c).getContentPane();
//                int cw = c.getWidth();
//                int ch = c.getHeight();
//                int cwm = c.getWidth() - cp.getWidth();
//                int chm = c.getHeight() - cp.getHeight();
//                cw -= cwm;
//                ch -= chm;
//                terminal.setSize(cw, ch);
//                textArea.setSize(cw, ch);
//            }
//        };
//        addComponentListener(l);

        applyConfig(configName);
        this.add(terminal);
        kick();
    }

    public void kick() {
        this.thread = new Thread(this);
        this.thread.start();
    }

    void applyConfig(String configName) {
        this.configName = configName;
        Configuration conf = JCTermSwing.getCR().load(configName);
        _setFontSize(conf.font_size);
        _setFgBg(conf.fg_bg[0]);
    }

    private void _setFontSize(int size) {
        int mwidth = frame.getWidth() - terminal.getTermWidth();
        int mheight = frame.getHeight() - terminal.getTermHeight();
        //terminal.setFont("Monospaced-" + size);
        frame.setSize(mwidth + terminal.getTermWidth(), mheight + terminal.getTermHeight());
        terminal.clear();
        terminal.redraw(0, 0, terminal.getWidth(), terminal.getHeight());
    }

    private void _setFgBg(String fg_bg) {
        String[] tmp = fg_bg.split(":");
        Color fg = toColor(tmp[0]);
        Color bg = toColor(tmp[1]);
        terminal.setForeGround(fg);
        terminal.setDefaultForeGround(fg);
        terminal.setBackGround(bg);
        terminal.setDefaultBackGround(bg);
        terminal.resetCursorGraphics();
        terminal.clear();
        terminal.redraw(0, 0, terminal.getWidth(), terminal.getHeight());
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    public void run() {
        String destination = null;
        while (thread != null) {
            try {
                int port = 22;
                try {
                    String[] destinations = JCTermSwing.getCR().load(configName).destinations;
                    String _host = "spiralinks@jboss-alex";
                    destination = _host;
                    if (_host == null) {
                        break;
                    }
                    String _user = _host.substring(0, _host.indexOf('@'));
                    _host = _host.substring(_host.indexOf('@') + 1);
                    if (_host == null || _host.length() == 0) {
                        continue;
                    }
                    if (_host.indexOf(':') != -1) {
                        try {
                            port = Integer.parseInt(_host.substring(_host.indexOf(':') + 1));
                        } catch (Exception eee) {
                        }
                        _host = _host.substring(0, _host.indexOf(':'));
                    }
                    user = _user;
                    host = _host;
                } catch (Exception ee) {
                    continue;
                }

                try {
                    UserInfo ui = new MyUserInfo();

                    jschsession = JSchSession.getSession(user, null, host, port, ui, proxy);
                    setCompression(compression);

                    Configuration conf = JCTermSwing.getCR().load(configName);
                    conf.addDestination(destination);
                    JCTermSwing.getCR().save(conf);
                } catch (Exception e) {
                    //System.out.println(e);
                    break;
                }

                Channel channel = null;
                OutputStream out = null;
                InputStream in = null;

                if (mode == SHELL) {
                    channel = jschsession.getSession().openChannel("shell");
                    if (xforwarding) {
                        jschsession.getSession().setX11Host(xhost);
                        jschsession.getSession().setX11Port(xport + 6000);
                        channel.setXForwarding(true);
                    }

                    out = channel.getOutputStream();
                    in = channel.getInputStream();

                    channel.connect();
                } else if (mode == SFTP) {

                    out = new PipedOutputStream();
                    in = new PipedInputStream();

                    channel = jschsession.getSession().openChannel("sftp");

                    channel.connect();

                    (new Sftp((ChannelSftp) channel, (InputStream) (new PipedInputStream(
                            (PipedOutputStream) out)), new PipedOutputStream(
                            (PipedInputStream) in))).kick();
                }

                final OutputStream fout = out;
                final InputStream fin = in;
                final Channel fchannel = channel;

                connection = new Connection() {
                    public InputStream getInputStream() {
                        return fin;
                    }

                    public OutputStream getOutputStream() {
                        return fout;
                    }

                    public void requestResize(Term term) {
                        if (fchannel instanceof ChannelShell) {
                            int c = term.getColumnCount();
                            int r = term.getRowCount();
                            ((ChannelShell) fchannel).setPtySize(c, r, c * term.getCharWidth(),
                                    r * term.getCharHeight());
                        }
                    }

                    public void close() {
                        fchannel.disconnect();
                    }
                };
                frame.setTitle("[" + (counter++) + "] " + user + "@" + host + (port != 22 ? (":" + new Integer(port).toString()) : ""));
                terminal.requestFocus();
                terminal.start(connection);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            break;
        }
        frame.setTitle("JCTerm");
        thread = null;

        dispose_connection();

//        if (getCloseOnExit()) {
//            frame.setVisible(false);
//            frame.dispose();
//        } else {
//            terminal.clear();
//            terminal.redraw(0, 0, terminal.getWidth(), terminal.getHeight());
//        }
    }

    void dispose_connection() {
        synchronized (this) {
            if (channel != null) {
                channel.disconnect();
                channel = null;
            }
        }
    }

    static java.awt.Color toColor(Object o) {
        if (o instanceof String) {
            try {
                return java.awt.Color.decode(((String) o).trim());
            } catch (java.lang.NumberFormatException e) {
            }
            return java.awt.Color.getColor(((String) o).trim());
        }
        if (o instanceof java.awt.Color) {
            return (java.awt.Color) o;
        }
        return Color.white;
    }

    @Override
    public void openFrame(int mode, String configName) {

    }

    @Override
    public void setTitle(String name) {

    }

    @Override
    public void dispose() {

    }

    public class MyUserInfo implements UserInfo, UIKeyboardInteractive {
        public boolean promptYesNo(String str) {
            Object[] options = {"yes", "no"};
            int foo = JOptionPane.showOptionDialog(terminal, str,
                    "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return foo == 0;
        }

        String passwd = null;
        String passphrase = null;
        JTextField pword = new JPasswordField(20);

        public String getPassword() {
            return passwd;
        }

        public String getPassphrase() {
            return passphrase;
        }

        public boolean promptPassword(String message) {
            Object[] ob = {pword};
            JPanel panel = new JPanel();
            panel.add(pword);
            pword.requestFocusInWindow();
            JOptionPane pane = new JOptionPane(panel,
                    JOptionPane.QUESTION_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION) {
                public void selectInitialValue() {
                }
            };

            JDialog dialog = pane.createDialog(terminal,
                    message);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
            Object o = pane.getValue();

            if (o != null && ((Integer) o).intValue() == JOptionPane.OK_OPTION) {
                passwd = pword.getText();
                return true;
            } else {
                return false;
            }
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public void showMessage(String message) {
            JOptionPane.showMessageDialog(null, message);
        }

        final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0,
                0, 0), 0, 0);
        private Container panel;

        public String[] promptKeyboardInteractive(String destination, String name,
                                                  String instruction, String[] prompt, boolean[] echo) {
            panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridx = 0;
            panel.add(new JLabel(instruction), gbc);
            gbc.gridy++;

            gbc.gridwidth = GridBagConstraints.RELATIVE;

            JTextField[] texts = new JTextField[prompt.length];
            for (int i = 0; i < prompt.length; i++) {
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridx = 0;
                gbc.weightx = 1;
                panel.add(new JLabel(prompt[i]), gbc);

                gbc.gridx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weighty = 1;
                if (echo[i]) {
                    texts[i] = new JTextField(20);
                } else {
                    texts[i] = new JPasswordField(20);
                    texts[i].requestFocusInWindow();
                }
                panel.add(texts[i], gbc);
                gbc.gridy++;
            }
            for (int i = prompt.length - 1; i > 0; i--) {
                texts[i].requestFocusInWindow();
            }
            JOptionPane pane = new JOptionPane(panel,
                    JOptionPane.QUESTION_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION) {
                public void selectInitialValue() {
                }
            };
            JDialog dialog = pane.createDialog(terminal,
                    destination + ": " + name);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
            Object o = pane.getValue();
            if (o != null && ((Integer) o).intValue() == JOptionPane.OK_OPTION) {
                String[] response = new String[prompt.length];
                for (int i = 0; i < prompt.length; i++) {
                    response[i] = texts[i].getText();
                }
                return response;
            } else {
                return null; // cancel
            }
        }
    }

    public void setCompression(int compression) {
        if (compression < 0 || 9 < compression)
            return;
        this.compression = compression;
        if (jschsession != null) {
            if (compression == 0) {
                jschsession.getSession().setConfig("compression.s2c", "none");
                jschsession.getSession().setConfig("compression.c2s", "none");
                jschsession.getSession().setConfig("compression_level", "0");
            } else {
                jschsession.getSession().setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
                jschsession.getSession().setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
                jschsession.getSession().setConfig("compression_level",
                        new Integer(compression).toString());
            }
            try {
                jschsession.getSession().rekey();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
