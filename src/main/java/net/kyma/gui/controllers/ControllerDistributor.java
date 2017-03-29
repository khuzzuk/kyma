package net.kyma.gui.controllers;

import javafx.fxml.Initializable;
import javafx.util.Callback;

import java.util.Map;

public class ControllerDistributor implements Callback<Class<?>, Object> {
    private Map<Class<?>, Initializable> controllers;
    @Override
    public Object call(Class<?> param) {
        return controllers.get(param);
    }
}
