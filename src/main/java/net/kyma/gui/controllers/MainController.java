package net.kyma.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;

import javax.inject.Singleton;
import java.net.URL;
import java.util.ResourceBundle;

@Singleton
public class MainController implements Initializable {
    @FXML
    private GridPane playerPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
