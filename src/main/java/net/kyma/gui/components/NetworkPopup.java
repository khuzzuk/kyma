package net.kyma.gui.components;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import net.kyma.EventType;
import pl.khuzzuk.messaging.Bus;

import java.net.MalformedURLException;
import java.net.URL;

@RequiredArgsConstructor
public class NetworkPopup {
    private final Bus<EventType> bus;
    private Stage window;
    private TextField importField;
    private Button importButton;

    public void init() {
        window = new Stage();

        importField = new TextField();
        importField.promptTextProperty().setValue("url");
        importButton = new Button("Import");
        importButton.setOnAction(event -> importUrl());

        VBox container = new VBox();
        container.getChildren().addAll(importField, importButton);

        Scene scene = new Scene(container);
        scene.getStylesheets().add("css/classic.css");
        window.setScene(scene);
    }

    public void show() {
        importField.clear();
        window.show();
    }

    public void importUrl() {
        try {
            URL url = new URL(importField.getText());
            bus.message(EventType.PLAYLIST_ADD_YOUTUBE).withContent(url).send();
            window.hide();
        } catch (MalformedURLException e) {
            bus.message(EventType.SHOW_ALERT).withContent(e.getMessage()).send();
        }
    }
}
