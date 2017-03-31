package net.kyma.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import net.kyma.gui.controllers.ControllerDistributor;

import javax.inject.Inject;
import java.io.IOException;

public class MainWindow extends Stage {
    private ControllerDistributor controllerDistributor;
    @Inject
    public MainWindow(ControllerDistributor controllerDistributor) {
        super(StageStyle.DECORATED);
        initModality(Modality.WINDOW_MODAL);
        this.controllerDistributor = controllerDistributor;
    }

    void initMainWindow(Window parent) {
        initOwner(parent);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mainWindow.fxml"));
        loader.setControllerFactory(controllerDistributor);
        try {
            setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
