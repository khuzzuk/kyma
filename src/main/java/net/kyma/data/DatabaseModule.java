package net.kyma.data;

import com.google.inject.AbstractModule;
import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@Log4j2
public class DatabaseModule extends AbstractModule {
    @Override
    protected void configure() {
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        bind(SessionFactory.class).toInstance(sessionFactory);
    }
}
