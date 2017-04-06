package net.kyma.data;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyma.dm.SoundFile;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import pl.khuzzuk.messaging.Bus;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaQuery;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@Singleton
public class DataIndexer {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    @Inject
    private SessionFactory factory;
    private Session session;

    @PostConstruct
    public void init() {
        bus.<File, SoundFile>setResponse(messages.getProperty("playlist.add.file"), SoundFile::from);
        bus.<Collection<File>>setReaction(messages.getProperty("data.index.list"), this::index);
        bus.setResponse(messages.getProperty("data.index.getAll"), this::getAll);
        session = factory.openSession();
    }

    private synchronized void index(Collection<File> files) {
        session.beginTransaction();
        files.parallelStream().onClose(() -> {
            session.getTransaction().commit();
            bus.send(messages.getProperty("data.index.getAll"), messages.getProperty("data.view.refresh"));
        }).map(SoundFile::from).forEach(this::persist);
    }

    private synchronized SoundFile index(File file) {
        SoundFile sound = SoundFile.from(file);
        persist(sound);
        return sound;
    }

    private void persist(SoundFile soundFile) {
        session.saveOrUpdate(soundFile);
    }

    private synchronized List<SoundFile> getAll() {
        CriteriaQuery<SoundFile> criteriaQuery = factory.getCriteriaBuilder().createQuery(SoundFile.class);
        criteriaQuery.from(SoundFile.class);
        session.beginTransaction();
        List<SoundFile> results = session.createQuery(criteriaQuery).getResultList();
        session.getTransaction().commit();
        return results;
    }
}
