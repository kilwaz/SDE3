package application.data;

import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.controlsfx.glyphfont.FontAwesome;

public class DatabaseConnectionWatcher extends HBox {
    private static DatabaseConnectionWatcher instance;
    private Label databaseIcon;
    private Label databaseDescription;
    private Boolean connected = false;

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

    public Boolean getConnected() {
        return connected;
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
                if (connected) { // Green
                    FontAwesome fontAwesome = new FontAwesome();
                    databaseIcon = new Label("", GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.DATABASE)
                            .style("-fx-fill: green;")
                            .size("1.2em")
                            .build());
                    databaseDescription = new Label("Connected!");
                } else { // Red
                    databaseIcon = new Label("", GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.DATABASE)
                            .style("-fx-fill: red;")
                            .size("1.2em")
                            .build());
                    databaseDescription = new Label("Not Connected!");
                }

                databaseConnectionWatcher.getChildren().clear();
                databaseConnectionWatcher.getChildren().add(databaseDescription);
                databaseConnectionWatcher.getChildren().add(databaseIcon);
            }
        }

        Platform.runLater(new GUIUpdate(connected, this));
    }
}
