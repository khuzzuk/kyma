package net.kyma.player;

import javafx.application.Application;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;

import java.io.File;

@NoArgsConstructor
public class Player extends Application {
    void play(String path) {
        System.out.println(path.toString());
        Media media = new Media(new File("D:/Java/kyma/Preludium.mp3").toURI().toString());
        MediaPlayer player = new MediaPlayer(media);
        player.setAutoPlay(true);
        MediaView view = new MediaView(player);
        player.play();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        play("");
    }
}
