package net.kyma.player;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.ToolFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import lombok.NoArgsConstructor;

import java.io.File;

@NoArgsConstructor
public class Player {
    void play(String path) {
        System.out.println(path.toString());
        Media media = new Media(new File("C:/Users/adria/IdeaProjects/kyma/Preludium.mp3").toURI().toString());
        MediaPlayer player = new MediaPlayer(media);
        player.setAutoPlay(true);
        MediaView view = new MediaView(player);
        player.play();
    }

    public static void main(String[] args) {
        IMediaReader reader = ToolFactory.makeReader("Preludium.mp3");
        reader.addListener(ToolFactory.makeViewer(IMediaViewer.Mode.AUDIO_ONLY));
        reader.readPacket();
    }
}
