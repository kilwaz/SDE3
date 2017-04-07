package sde.application.gui.update.switchnode;

import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;

public class ToggleButtonImage implements Runnable {
    private ToggleButton button;

    public ToggleButtonImage(ToggleButton button) {
        this.button = button;
    }

    @Override
    public void run() {
        Node icon;
        if (button.isSelected()) {
            icon = GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.CHECK).build();
        } else {
            icon = GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.CLOSE).build();
        }

        button.setGraphic(icon);
    }
}
