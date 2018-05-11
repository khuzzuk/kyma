package net.kyma.gui.controllers;

import java.util.HashMap;
import java.util.Map;

import javafx.fxml.Initializable;
import javafx.util.Callback;

public class ControllerDistributor implements Callback<Class<?>, Object> {
    private final Map<Class<?>, Initializable> controllers;

    public ControllerDistributor(MainController controller,
                                 PlayerPaneController playerPaneController,
                                 ManagerPaneController managerPaneController,
                                 ContentView contentView) {
        this.controllers = new HashMap<>();
        controllers.put(MainController.class, controller);
        controllers.put(PlayerPaneController.class, playerPaneController);
        controllers.put(ManagerPaneController.class, managerPaneController);
        controllers.put(ContentView.class, contentView);
    }

    @Override
    public Object call(Class<?> param) {
        return controllers.get(param);
    }

    public MainController getMainController() {
        return (MainController) controllers.get(MainController.class);
    }
}
