package net.kyma.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.kyma.gui.controllers.MainController;
import net.kyma.gui.manager.ManagerPane;

class MainPane extends SplitPane {
    private MenuItem openFile = new MenuItem("Otwórz plik");
    private MenuItem indexDirectory = new MenuItem("Indeksuj katalog");
    private Menu mainMenu = new Menu("Plik", null, openFile, indexDirectory);
    private MenuBar mainMenuBar = new MenuBar(mainMenu);

    private final ProgressIndicator indicator = new ProgressIndicator();
    private GridPane playerPane = new GridPane();
    private HBox playerBox = new HBox();
    private final ManagerPane managerPane;

    private final MainController controller;

    MainPane(ManagerPane managerPane, MainController controller, Stage stage) {
        this.managerPane = managerPane;
        this.controller = controller;

        //getChildren().addAll(mainMenuBar, this.managerPane, playerBox);
        getChildren().addAll(mainMenuBar, new Button("BUTTON"), new Button("1"));
        setDividerPositions(0.2d, 0.85d);
/*
        setOrientation(Orientation.VERTICAL);
        setMinHeight(Double.MAX_VALUE);
        setMinWidth(Double.MAX_VALUE);
*/

        playerBox.setAlignment(Pos.CENTER);
        playerBox.setSpacing(5);
        playerBox.setMaxHeight(Double.MAX_VALUE);
        playerBox.getChildren().addAll(indicator, playerPane);

        controller.setIndicator(indicator);
        controller.setStage(stage);
        controller.initialize();
    }
}
