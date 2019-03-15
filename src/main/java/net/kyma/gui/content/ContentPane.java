package net.kyma.gui.content;

import javafx.scene.control.TableView;
import net.kyma.dm.SoundFile;

public class ContentPane extends TableView<SoundFile> {
    public ContentPane(ContentPaneController contentPaneController) {
        setEditable(true);

        setOnMouseClicked(contentPaneController::onMouseEvent);
        setOnKeyReleased(contentPaneController::onKeyReleased);

        contentPaneController.setMainContentView(this);
        contentPaneController.initialize();
    }
}
