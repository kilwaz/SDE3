package main;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.InputStream;
import java.io.OutputStream;

//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.awt.image.BufferedImage;

public class JCTermJavaFx extends Canvas implements Term {
    OutputStream out;
    InputStream in;
    Emulator emulator = null;

    Connection connection = null;

    private WritableImage img;
    private WritableImage background;
    private GraphicsContext cursor_graphics;
    private GraphicsContext graphics;
    private Color defaultbground = Color.BLACK;
    private Color bground = Color.BLACK;
    private Color fground = Color.WHITE;
    private Canvas term_area = null;
    private Font font;

    private boolean bold = false;
    private boolean underline = false;
    private boolean reverse = false;

    private int term_width = 80;
    private int term_height = 24;

    private int descent = 0;

    private int x = 0;
    private int y = 0;

    private int char_width;
    private int char_height;

    //private int line_space=0;
    private int line_space = -2;
    private int compression = 0;

    private boolean antialiasing = true;

    private Splash splash = null;

    private final Object[] colors = {Color.BLACK, Color.RED, Color.GREEN,
            Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.WHITE};

    public JCTermJavaFx() {
        //enableEvents(AWTEvent.KEY_EVENT_MASK);

        graphics = this.getGraphicsContext2D();

        this.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                System.out.println("Pressed a key on the canvas");
            }
        });

        setFont("Monospaced-14");

        setWidth(getTermWidth());
        setHeight(getTermHeight());

        if (splash != null) {
            splash.draw(img, getTermWidth(), getTermHeight());
        } else {
            clear();
        }

        term_area = this;

        prefHeight(getTermHeight());
        prefWidth(getTermWidth());

        setFocused(true);
        setFocusTraversable(true);
        //enableInputMethods(true);

        //setFocusTraversalKeysEnabled(false);
        //  setOpaque(true);
    }


    public void start(Connection connection) {
        this.connection = connection;
        in = connection.getInputStream();
        out = connection.getOutputStream();
        emulator = new EmulatorVT100(this, in);
        emulator.reset();
        emulator.start();

        if (splash != null) {
            splash.draw(img, getTermWidth(), getTermHeight());
        } else {
            clear();
        }
        redraw(0, 0, getTermWidth(), getTermHeight());
    }

    @Override
    public int getRowCount() {
        return term_height;
    }

    @Override
    public int getColumnCount() {
        return term_width;
    }

    @Override
    public int getCharWidth() {
        return char_width;
    }

    @Override
    public int getCharHeight() {
        return char_height;
    }

    @Override
    public void setCursor(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void clear() {
        graphics.setFill(bground);
        graphics.fillRect(0, 0, char_width * term_width, char_height * term_height);
        graphics.setFill(fground);
    }

    @Override
    public void draw_cursor() {

    }

    @Override
    public void redraw(int x, int y, int width, int height) {

    }

    @Override
    public void clear_area(int x1, int y1, int x2, int y2) {
        graphics.setFill(bground);
        graphics.fillRect(x1, y1, x2 - x1, y2 - y1);
        graphics.setFill(fground);
    }

    @Override
    public void scroll_area(int x, int y, int w, int h, int dx, int dy) {
//        graphics.fill
//        graphics.copyArea(x, y, w, h, dx, dy);
//        repaint(x + dx, y + dy, w, h);
    }

    @Override
    public void drawBytes(byte[] buf, int s, int len, int x, int y) {

    }

    @Override
    public void drawString(String str, int x, int y) {

    }

    @Override
    public void beep() {

    }

    @Override
    public void setDefaultForeGround(Object foreground) {

    }

    @Override
    public void setDefaultBackGround(Object background) {

    }

    @Override
    public void setForeGround(Object foreground) {

    }

    @Override
    public void setBackGround(Object background) {

    }

    @Override
    public void setBold() {

    }

    @Override
    public void setUnderline() {

    }

    @Override
    public void setReverse() {

    }

    @Override
    public void resetAllAttributes() {

    }

    @Override
    public int getTermWidth() {
        return char_width * term_width;
    }

    @Override
    public int getTermHeight() {
        return char_height * term_height;
    }

    @Override
    public Object getColor(int index) {
        return null;
    }

    void setFont(String fname) {
        Font font = Font.getDefault();
//        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
//        Graphics2D graphics = (Graphics2D) (img.getGraphics());
//        graphics.setFont(font);
        {
            FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
            descent = (int) metrics.getDescent();
            char_width = (int) metrics.computeStringWidth("@");
            char_height = (int) (metrics.getLineHeight()) + (line_space * 2);
            descent += line_space;
        }

//        img.flush();
//        graphics.dispose();

        //, BufferedImage.TYPE_INT_RGB

        Canvas canvas = new Canvas(char_width, char_height);
        //background = new BufferedImage(char_width, char_height, BufferedImage.TYPE_INT_RGB);
        {
            GraphicsContext foog = canvas.getGraphicsContext2D();
            foog.setFill(Color.BLACK);
            foog.fillRect(0, 0, char_width, char_height);
            background = canvas.snapshot(null, null);
        }
    }
//
//    public void setSize(int w, int h) {
//
//        super.setSize(w, h);
//        BufferedImage imgOrg = img;
//        if (graphics != null)
//            graphics.dispose();
//
//        int column = w / getCharWidth();
//        int row = h / getCharHeight();
//        term_width = column;
//        term_height = row;
//
//        if (emulator != null)
//            emulator.reset();
//
//        img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
//        graphics = (Graphics2D) (img.getGraphics());
//        graphics.setFont(font);
//
//        clear_area(0, 0, w, h);
//        redraw(0, 0, w, h);
//
//        if (imgOrg != null) {
//            Shape clip = graphics.getClip();
//            graphics.setClip(0, 0, getTermWidth(), getTermHeight());
//            graphics.drawImage(imgOrg, 0, 0, term_area);
//            graphics.setClip(clip);
//        }
//
//        resetCursorGraphics();
//
//        setAntiAliasing(antialiasing);
//
//        if (connection != null) {
//            connection.requestResize(this);
//        }
//
//        if (imgOrg != null) {
//            imgOrg.flush();
//            imgOrg = null;
//        }
//    }
//
//    public void start(Connection connection) {
//        this.connection = connection;
//        in = connection.getInputStream();
//        out = connection.getOutputStream();
//        emulator = new EmulatorVT100(this, in);
//        emulator.reset();
//        emulator.start();
//
//        if (splash != null)
//            splash.draw(img, getTermWidth(), getTermHeight());
//        else
//            clear();
//        redraw(0, 0, getTermWidth(), getTermHeight());
//    }
//
//    public void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        if (img != null) {
//            g.drawImage(img, 0, 0, term_area);
//        }
//    }
//
//    public void paint(Graphics g) {
//        super.paint(g);
//    }
//
//    public void processKeyEvent(KeyEvent e) {
//        //System.out.println(e);
//        int id = e.getID();
//        if (id == KeyEvent.KEY_PRESSED) {
//            keyPressed(e);
//        } else if (id == KeyEvent.KEY_RELEASED) {
//      /*keyReleased(e);*/
//        } else if (id == KeyEvent.KEY_TYPED) {
//            keyTyped(e);/*keyTyped(e);*/
//        }
//        e.consume(); // ??
//    }
//
//    byte[] obuffer = new byte[3];
//
//    public void keyPressed(KeyEvent e) {
//        int keycode = e.getKeyCode();
//        byte[] code = null;
//        switch (keycode) {
//            case KeyEvent.VK_CONTROL:
//            case KeyEvent.VK_SHIFT:
//            case KeyEvent.VK_ALT:
//            case KeyEvent.VK_CAPS_LOCK:
//                return;
//            case KeyEvent.VK_ENTER:
//                code = emulator.getCodeENTER();
//                break;
//            case KeyEvent.VK_UP:
//                code = emulator.getCodeUP();
//                break;
//            case KeyEvent.VK_DOWN:
//                code = emulator.getCodeDOWN();
//                break;
//            case KeyEvent.VK_RIGHT:
//                code = emulator.getCodeRIGHT();
//                break;
//            case KeyEvent.VK_LEFT:
//                code = emulator.getCodeLEFT();
//                break;
//            case KeyEvent.VK_F1:
//                code = emulator.getCodeF1();
//                break;
//            case KeyEvent.VK_F2:
//                code = emulator.getCodeF2();
//                break;
//            case KeyEvent.VK_F3:
//                code = emulator.getCodeF3();
//                break;
//            case KeyEvent.VK_F4:
//                code = emulator.getCodeF4();
//                break;
//            case KeyEvent.VK_F5:
//                code = emulator.getCodeF5();
//                break;
//            case KeyEvent.VK_F6:
//                code = emulator.getCodeF6();
//                break;
//            case KeyEvent.VK_F7:
//                code = emulator.getCodeF7();
//                break;
//            case KeyEvent.VK_F8:
//                code = emulator.getCodeF8();
//                break;
//            case KeyEvent.VK_F9:
//                code = emulator.getCodeF9();
//                break;
//            case KeyEvent.VK_F10:
//                code = emulator.getCodeF10();
//                break;
//            case KeyEvent.VK_TAB:
//                code = emulator.getCodeTAB();
//                break;
//        }
//        if (code != null) {
//            try {
//                out.write(code, 0, code.length);
//                out.flush();
//            } catch (Exception ee) {
//            }
//            return;
//        }
//
//        char keychar = e.getKeyChar();
//        if ((keychar & 0xff00) == 0) {
//            obuffer[0] = (byte) (e.getKeyChar());
//            try {
//                out.write(obuffer, 0, 1);
//                out.flush();
//            } catch (Exception ee) {
//            }
//        }
//    }
//
//    public void keyTyped(KeyEvent e) {
//        char keychar = e.getKeyChar();
//        if ((keychar & 0xff00) != 0) {
//            char[] foo = new char[1];
//            foo[0] = keychar;
//            try {
//                byte[] goo = new String(foo).getBytes("EUC-JP");
//                out.write(goo, 0, goo.length);
//                out.flush();
//            } catch (Exception eee) {
//            }
//        }
//    }
//
//
//    public int getCharWidth() {
//        return char_width;
//    }
//
//    public int getCharHeight() {
//        return char_height;
//    }
//
//    public int getColumnCount() {
//        return term_width;
//    }
//
//    public int getRowCount() {
//        return term_height;
//    }
//
//    public void clear() {
//        graphics.setColor(getBackGround());
//        graphics.fillRect(0, 0, char_width * term_width, char_height * term_height);
//        graphics.setColor(getForeGround());
//    }
//
//    public void setCursor(int x, int y) {
//        //System.out.println("setCursor: "+x+","+y);
//        this.x = x;
//        this.y = y;
//    }
//
//    public void draw_cursor() {
//        cursor_graphics.fillRect(x, y - char_height, char_width, char_height);
//        repaint(x, y - char_height, char_width, char_height);
//    }
//
//    public void redraw(int x, int y, int width, int height) {
//        repaint(x, y, width, height);
//    }
//
//    public void clear_area(int x1, int y1, int x2, int y2) {
//        //System.out.println("clear_area: "+x1+" "+y1+" "+x2+" "+y2);
//        graphics.setColor(getBackGround());
//        graphics.fillRect(x1, y1, x2 - x1, y2 - y1);
//        graphics.setColor(getForeGround());
//    }
//
//    public void scroll_area(int x, int y, int w, int h, int dx, int dy) {
//        //System.out.println("scroll_area: "+x+" "+y+" "+w+" "+h+" "+dx+" "+dy);
//        graphics.copyArea(x, y, w, h, dx, dy);
//        repaint(x + dx, y + dy, w, h);
//    }
//
//    public void drawBytes(byte[] buf, int s, int len, int x, int y) {
//        //    clear_area(x, y, x+len*char_width, y+char_height);
//        //    graphics.setColor(getForeGround());
//
//        //System.out.println("drawString: "+x+","+y+" "+len+" "+new String(buf, s, len));
//
//        graphics.drawBytes(buf, s, len, x, y - descent);
//        if (bold)
//            graphics.drawBytes(buf, s, len, x + 1, y - descent);
//
//        if (underline) {
//            graphics.drawLine(x, y - 1, x + len * char_width, y - 1);
//        }
//
//    }
//
//    public void drawString(String str, int x, int y) {
//        //    clear_area(x, y, x+str.length()*char_width, y+char_height);
//        //    graphics.setColor(getForeGround());
//        graphics.drawString(str, x, y - descent);
//        if (bold)
//            graphics.drawString(str, x + 1, y - descent);
//
//        if (underline) {
//            graphics.drawLine(x, y - 1, x + str.length() * char_width, y - 1);
//        }
//
//    }
//
//    public void beep() {
//        Toolkit.getDefaultToolkit().beep();
//    }
//
//    /**
//     * Ignores key released events.
//     */
//    public void keyReleased(KeyEvent event) {
//    }
//
//    //  public void keyPressed(KeyEvent event){}
//
//    public void setSplash(Splash foo) {
//        this.splash = foo;
//    }
//
//    public void setLineSpace(int foo) {
//        this.line_space = foo;
//    }
//
//    public boolean getAntiAliasing() {
//        return antialiasing;
//    }
//
//    public void setAntiAliasing(boolean foo) {
//        if (graphics == null)
//            return;
//        antialiasing = foo;
//        java.lang.Object mode = foo ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
//                : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
//        RenderingHints hints = new RenderingHints(
//                RenderingHints.KEY_TEXT_ANTIALIASING, mode);
//        graphics.setRenderingHints(hints);
//    }
//
//    public void setCompression(int compression) {
//        if (compression < 0 || 9 < compression)
//            return;
//        this.compression = compression;
//    }
//
//    static java.awt.Color toColor(Object o) {
//        if (o instanceof String) {
//            try {
//                return java.awt.Color.decode(((String) o).trim());
//            } catch (java.lang.NumberFormatException e) {
//            }
//            return java.awt.Color.getColor(((String) o).trim());
//        }
//        if (o instanceof java.awt.Color) {
//            return (java.awt.Color) o;
//        }
//        return Color.white;
//    }
//
//    public void setDefaultForeGround(Object f) {
//        defaultfground = toColor(f);
//    }
//
//    public void setDefaultBackGround(Object f) {
//        defaultbground = toColor(f);
//    }
//
//    public void setForeGround(Object f) {
//        fground = toColor(f);
//        graphics.setColor(getForeGround());
//    }
//
//    public void setBackGround(Object b) {
//        bground = toColor(b);
//        Graphics2D foog = (Graphics2D) (background.getGraphics());
//        foog.setColor(getBackGround());
//        foog.fillRect(0, 0, char_width, char_height);
//        foog.dispose();
//    }
//
//    private java.awt.Color getForeGround() {
//        if (reverse)
//            return bground;
//        return fground;
//    }
//
//    private java.awt.Color getBackGround() {
//        if (reverse)
//            return fground;
//        return bground;
//    }
//
//    public void resetCursorGraphics() {
//        if (cursor_graphics != null)
//            cursor_graphics.dispose();
//
//        cursor_graphics = (Graphics2D) (img.getGraphics());
//        cursor_graphics.setColor(getForeGround());
//        cursor_graphics.setXORMode(getBackGround());
//    }
//
//    public Object getColor(int index) {
//        if (colors == null || index < 0 || colors.length <= index)
//            return null;
//        return colors[index];
//    }
//
//    public void setBold() {
//        bold = true;
//    }
//
//    public void setUnderline() {
//        underline = true;
//    }
//
//    public void setReverse() {
//        reverse = true;
//        if (graphics != null)
//            graphics.setColor(getForeGround());
//    }
//
//    public void resetAllAttributes() {
//        bold = false;
//        underline = false;
//        reverse = false;
//        bground = defaultbground;
//        fground = defaultfground;
//        if (graphics != null)
//            graphics.setColor(getForeGround());
//    }
//
//    private static ConfigurationRepository defaultCR =
//            new ConfigurationRepository() {
//                private final Configuration conf = new Configuration();
//
//                public Configuration load(String name) {
//                    return conf;
//                }
//
//                public void save(Configuration conf) {
//                }
//            };
//
//    private static ConfigurationRepository cr = defaultCR;
//
//    public static synchronized void setCR(ConfigurationRepository _cr) {
//        if (_cr == null)
//            _cr = defaultCR;
//        cr = _cr;
//    }
//
//    public static synchronized ConfigurationRepository getCR() {
//        return cr;
//    }
}
