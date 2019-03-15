package net.kyma.gui;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import net.kyma.gui.manager.ManagerPane;
import net.kyma.gui.player.PlayerPane;

class MainPane extends SplitPane {
    MainPane(ManagerPane managerPane, PlayerPane playerPane, MainController controller, Stage stage) {
        getStyleClass().add("BackgroundContainer");

        setOrientation(Orientation.VERTICAL);
        setDividerPositions(0, 0.85);

        ProgressIndicator indicator = new ProgressIndicator();

        HBox.setHgrow(playerPane, Priority.ALWAYS);
        HBox playerBox = new HBox(playerPane, indicator);
        playerBox.setAlignment(Pos.CENTER);
        playerBox.setSpacing(5);
        playerBox.setMaxHeight(Double.MAX_VALUE);

        MenuItem openFile = new MenuItem("Otwórz plik");
        openFile.setOnAction(event -> controller.openFile());
        MenuItem indexDirectory = new MenuItem("Indeksuj katalog");
        indexDirectory.setOnAction(event -> controller.indexDirectory());

        MenuBar mainMenuBar = new MenuBar(
                new Menu("Plik", null,
                        openFile, indexDirectory));

        getItems().addAll(
                mainMenuBar,
                managerPane,
                playerBox
        );

        indicator.setVisible(false);

        controller.setIndicator(indicator);
        controller.setStage(stage);
        controller.initialize();
    }
}
