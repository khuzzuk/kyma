package net.kyma.gui;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import net.kyma.BusModule;
import net.kyma.data.DataIndexer;
import net.kyma.data.DatabaseModule;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;

@Log4j2
public class Manager extends Application {
    private static Injector injector;
    public static void main(String[] args) {
        injector = Guice.createInjector(new ControllersModule(), new BusModule(), new DatabaseModule());
        //TODO initialize it on the BUS and don't spam it on starting thread
        Session session = injector.getInstance(SessionFactory.class).openSession();
        FullTextSession fullTextSession = Search.getFullTextSession(session);
        try {
            fullTextSession.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            log.error("Database desynchronized");
            log.error(e);
        }
        fullTextSession.close();
        session.close();
        injector.getInstance(DataIndexer.class).init();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainWindow window = injector.getInstance(MainWindow.class);
        window.initMainWindow(primaryStage);
        window.show();
    }
}
