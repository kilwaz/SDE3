package application.data;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

public class DatabaseConnectionWatcher extends HBox {
    private Label databaseIcon;
    private Label databaseDescription;
    private Boolean connected = false;

    private static DatabaseConnectionWatcher instance;

    public DatabaseConnectionWatcher() {
        instance = this;
        setConnected(false);
        setSpacing(5);
    }

    public static DatabaseConnectionWatcher getInstance() {
        if (instance == null) {
            new DatabaseConnectionWatcher();
        }
        return instance;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;

        class GUIUpdate implements Runnable {
            private Boolean connected;
            private DatabaseConnectionWatcher databaseConnectionWatcher;

            GUIUpdate(Boolean connected, DatabaseConnectionWatcher databaseConnectionWatcher) {
                this.connected = connected;
                this.databaseConnectionWatcher = databaseConnectionWatcher;
            }

            public void run() {
                if (connected) {
                    GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
                    databaseIcon = new Label("", fontAwesome.create(FontAwesome.Glyph.DATABASE).color(Color.GREEN));
                    databaseDescription = new Label("Connected!");
                } else {
                    GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
                    databaseIcon = new Label("", fontAwesome.create(FontAwesome.Glyph.DATABASE).color(Color.RED));
                    databaseDescription = new Label("Not Connected!");
                }

                databaseConnectionWatcher.getChildren().clear();
                databaseConnectionWatcher.getChildren().add(databaseDescription);
                databaseConnectionWatcher.getChildren().add(databaseIcon);
            }
        }

        Platform.runLater(new GUIUpdate(connected, this));
    }

    public Boolean getConnected() {
        return connected;
    }
}
