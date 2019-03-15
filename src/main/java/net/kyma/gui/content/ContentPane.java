package net.kyma.gui.content;

import javafx.scene.control.TableView;
import net.kyma.dm.SoundFile;

public class ContentPane extends TableView<SoundFile> {
    private final ContentPaneController contentPaneController;

    public ContentPane(ContentPaneController contentPaneController) {
        this.contentPaneController = contentPaneController;

        setEditable(true);

        setOnMouseClicked(contentPaneController::onMouseEvent);
        setOnKeyReleased(contentPaneController::onKeyReleased);
    }
}
