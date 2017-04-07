package sde.application.gui.dialog;

public interface Dialog {
    public Dialog content(String content);

    public Dialog title(String title);

    public Dialog header(String header);

    public void show();
}
