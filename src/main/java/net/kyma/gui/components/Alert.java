package net.kyma.gui.components;

import static net.kyma.EventType.SHOW_ALERT;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.kyma.EventType;
import net.kyma.Loadable;
import pl.khuzzuk.messaging.Bus;

public class Alert implements Loadable {
   private final Bus<EventType> bus;
   private Stage alertMessagesStage;
   private TextArea messages;

   public Alert(Bus<EventType> bus){
      this.bus = bus;
   }

   @Override
   public void load() {
      Platform.runLater(this::initialize);
   }

   private void initialize() {
      alertMessagesStage = new Stage();
      alertMessagesStage.setTitle("Alert messages");

      messages = new TextArea();
      Button closeButton = new Button("Close");
      closeButton.getStyleClass().add("play-text");
      closeButton.setOnMouseClicked(event -> {
         alertMessagesStage.hide();
         messages.clear();
      });

      VBox container = new VBox();
      container.getChildren().addAll(messages, closeButton);

      Scene scene = new Scene(container);
      scene.getStylesheets().add("css/classic.css");
      alertMessagesStage.setScene(scene);

      bus.subscribingFor(SHOW_ALERT).onFXThread().accept(this::showMessage).subscribe();
   }

   private synchronized void showMessage(String text) {
      messages.setText(messages.getText() + text + "\n");
      alertMessagesStage.show();
   }
}
