package net.kyma.gui.controllers;

import javafx.fxml.Initializable;
import javafx.util.Callback;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ControllerDistributor implements Callback<Class<?>, Object> {
    private final Map<Class<?>, Initializable> controllers;

    @Inject
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
}
