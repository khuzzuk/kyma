package net.kyma.data;

import com.google.inject.AbstractModule;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

@Log4j2
public class DatabaseModule extends AbstractModule {
    @Override
    protected void configure() {
        try {
            Directory directory = new NIOFSDirectory(Paths.get("index/"));
            bind(Directory.class).toInstance(directory);

            IndexWriterConfig config = new IndexWriterConfig();
            config.setRAMBufferSizeMB(64);
            IndexWriter writer = new IndexWriter(directory, config);
            bind(IndexWriter.class).toInstance(writer);

        } catch (IOException e) {
            log.error("No access to database");
            log.error(e);
            System.err.println("Exiting");
            System.exit(1);
        }
    }
}
