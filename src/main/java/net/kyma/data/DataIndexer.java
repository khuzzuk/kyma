package net.kyma.data;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyma.dm.SoundFile;
import pl.khuzzuk.messaging.Bus;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.io.File;
import java.util.Properties;

@Singleton
public class DataIndexer {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;

    @PostConstruct
    public void init() {
        bus.<File, SoundFile>setResponse(messages.getProperty("playlist.add.file"), SoundFile::from);
    }
}
